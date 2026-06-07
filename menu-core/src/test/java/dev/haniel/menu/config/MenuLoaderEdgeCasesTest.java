package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MenuId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Adversarial edge-case probes for {@link MenuLoader}: malformed/empty YAML, missing required
 * fields, wrong types, out-of-range values, cache staleness and reload detection.
 *
 * <p>The documented contract of {@link MenuLoader#load} is that a missing, empty or malformed file
 * fails with {@link InvalidMenuException} aimed at the server owner, never a raw library error,
 * {@code NullPointerException} or {@code IllegalArgumentException}. These tests assert that
 * intended behaviour.
 */
class MenuLoaderEdgeCasesTest {

  private static final String VALID =
      """
      title: "Hi"
      rows: 3
      buttons:
        buy:
          slot: 13
          material: EMERALD
          name: "<green>Buy</green>"
      """;

  private static MenuConfig load(Path dir, String name, String yaml) throws IOException {
    Files.writeString(dir.resolve(name + ".yml"), yaml);
    return new MenuLoader(dir).load(new MenuId(name));
  }

  // ---------------------------------------------------------------------------------------------
  // Malformed / empty input
  // ---------------------------------------------------------------------------------------------

  @Test
  void rejectsMalformedYamlAsDomainException(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("broken.yml"), "title: \"Hi\nrows: 3\n  : : :");
    InvalidMenuException thrown =
        assertThrows(
            InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("broken")));
    assertTrue(thrown.getMessage().contains("broken"));
  }

  @Test
  void rejectsEmptyFile(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("empty.yml"), "");
    assertThrows(InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("empty")));
  }

  @Test
  void rejectsWhitespaceOnlyFile(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("blank.yml"), "   \n\n  \n");
    assertThrows(InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("blank")));
  }

  @Test
  void rejectsCommentOnlyFile(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("comment.yml"), "# just a comment, no real config\n");
    assertThrows(
        InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("comment")));
  }

  // ---------------------------------------------------------------------------------------------
  // Missing required fields
  // ---------------------------------------------------------------------------------------------

  @Test
  void rejectsMissingRows(@TempDir Path dir) {
    // No rows: the record cannot be built with a legal value, so the load must fail cleanly.
    assertThrows(
        InvalidMenuException.class,
        () -> load(dir, "norows", "title: \"Hi\"\nbuttons: {}\n"));
  }

  @Test
  void missingTitleDefaultsToEmpty(@TempDir Path dir) throws IOException {
    MenuConfig config = load(dir, "notitle", "rows: 3\nbuttons: {}\n");
    assertEquals("", config.title());
  }

  @Test
  void missingButtonsDefaultsToEmpty(@TempDir Path dir) throws IOException {
    MenuConfig config = load(dir, "nobuttons", "title: \"Hi\"\nrows: 2\n");
    assertTrue(config.buttons().isEmpty());
  }

  @Test
  void missingButtonSlotDefaultsToZero(@TempDir Path dir) throws IOException {
    // A genuinely absent slot must coalesce to the legal slot 0, not crash the load.
    MenuConfig config =
        load(
            dir,
            "noslot",
            """
            title: "Hi"
            rows: 1
            buttons:
              x:
                material: EMERALD
            """);
    assertEquals(0, config.buttons().get("x").slot());
    assertEquals("EMERALD", config.buttons().get("x").material());
  }

  // ---------------------------------------------------------------------------------------------
  // Wrong types
  // ---------------------------------------------------------------------------------------------

  @Test
  void rejectsNonNumericRows(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () -> load(dir, "strrows", "title: \"Hi\"\nrows: \"three\"\nbuttons: {}\n"));
  }

  @Test
  void rejectsNonNumericSlot(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () ->
            load(
                dir,
                "strslot",
                """
                title: "Hi"
                rows: 1
                buttons:
                  x:
                    slot: "middle"
                    material: EMERALD
                """));
  }

  @Test
  void rejectsButtonsDeclaredAsSequence(@TempDir Path dir) {
    // SUSPECTED BUG: 'buttons' written as a YAML sequence instead of a map is silently coalesced
    // to zero buttons, so the entire (misconfigured) buttons section vanishes with no error. A
    // server owner who mistypes the structure gets an empty menu rather than a clear failure. The
    // intended behaviour is to surface the structural mismatch as an InvalidMenuException.
    assertThrows(
        InvalidMenuException.class,
        () ->
            load(
                dir,
                "listbuttons",
                "title: \"Hi\"\nrows: 1\nbuttons:\n  - slot: 0\n  - slot: 1\n"));
  }

  // ---------------------------------------------------------------------------------------------
  // Out-of-range values
  // ---------------------------------------------------------------------------------------------

  @Test
  void rejectsRowsZero(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () -> load(dir, "rows0", "title: \"Hi\"\nrows: 0\nbuttons: {}\n"));
  }

  @Test
  void rejectsRowsAboveSix(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () -> load(dir, "rows7", "title: \"Hi\"\nrows: 7\nbuttons: {}\n"));
  }

  @Test
  void rejectsNegativeRows(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () -> load(dir, "rowsneg", "title: \"Hi\"\nrows: -1\nbuttons: {}\n"));
  }

  @Test
  void rejectsNegativeSlot(@TempDir Path dir) {
    assertThrows(
        InvalidMenuException.class,
        () ->
            load(
                dir,
                "negslot",
                """
                title: "Hi"
                rows: 1
                buttons:
                  x:
                    slot: -5
                    material: EMERALD
                """));
  }

  @Test
  void rejectsSlotBeyondMenuSize(@TempDir Path dir) {
    // rows: 1 -> valid slots are 0..8; slot 9 is out of range.
    assertThrows(
        InvalidMenuException.class,
        () ->
            load(
                dir,
                "bigslot",
                """
                title: "Hi"
                rows: 1
                buttons:
                  x:
                    slot: 9
                    material: EMERALD
                """));
  }

  // ---------------------------------------------------------------------------------------------
  // Unicode / whitespace in names
  // ---------------------------------------------------------------------------------------------

  @Test
  void preservesUnicodeAndWhitespaceInTitleAndName(@TempDir Path dir) throws IOException {
    MenuConfig config =
        load(
            dir,
            "unicode",
            """
            title: "  Café éé 你好  "
            rows: 1
            buttons:
              x:
                slot: 0
                material: EMERALD
                name: "  spaced é name  "
            """);
    assertEquals("  Café éé 你好  ", config.title());
    assertEquals("  spaced é name  ", config.buttons().get("x").name());
  }

  // ---------------------------------------------------------------------------------------------
  // Duplicate keys
  // ---------------------------------------------------------------------------------------------

  @Test
  void lastDuplicateTopLevelKeyWins(@TempDir Path dir) throws IOException {
    // YAML with a duplicated key: the loader must not crash. SnakeYAML keeps the last value.
    MenuConfig config = load(dir, "dupe", "title: \"Hi\"\nrows: 2\nrows: 4\nbuttons: {}\n");
    assertEquals(4, config.rows());
  }

  // ---------------------------------------------------------------------------------------------
  // Cache staleness and reload detection
  // ---------------------------------------------------------------------------------------------

  @Test
  void reparsesWhenContentChangesButModifiedTimeIsPreserved(@TempDir Path dir) throws IOException {
    // Some editors rewrite a file while restoring its original mtime. Content-aware invalidation
    // (size/checksum) must still notice the change even when mtime is identical.
    Path file = dir.resolve("hello.yml");
    Files.writeString(file, VALID);
    FileTime original = Files.getLastModifiedTime(file);

    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");
    MenuConfig first = loader.load(id);

    Files.writeString(file, VALID.replace("rows: 3", "rows: 5"));
    Files.setLastModifiedTime(file, original);

    MenuConfig second = loader.load(id);
    assertNotSame(first, second);
    assertEquals(5, second.rows());
  }

  @Test
  void reparsesWhenContentChangesButSizeIsIdentical(@TempDir Path dir) throws IOException {
    // A one-character edit that keeps the byte count identical: rows 3 -> rows 4. Pure size-based
    // invalidation would miss this; checksum-based invalidation must catch it.
    Path file = dir.resolve("hello.yml");
    Files.writeString(file, VALID);

    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");
    MenuConfig first = loader.load(id);
    assertEquals(3, first.rows());

    Files.writeString(file, VALID.replace("rows: 3", "rows: 4"));
    Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + 5_000));

    MenuConfig second = loader.load(id);
    assertEquals(4, second.rows());
  }

  @Test
  void stillServesCachedConfigAfterFileIsDeleted(@TempDir Path dir) throws IOException {
    // Once cached, a transient delete should not be silently served as stale config: the next
    // load must surface the failure rather than return the old config or NPE.
    Path file = dir.resolve("hello.yml");
    Files.writeString(file, VALID);
    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");
    loader.load(id);

    Files.delete(file);

    assertThrows(InvalidMenuException.class, () -> loader.load(id));
  }

  @Test
  void doesNotCacheConfigThatFailedValidation(@TempDir Path dir) throws IOException {
    // First load fails validation (slot out of range). After fixing the file, the next load must
    // succeed -- a poisoned cache entry would keep throwing.
    Path file = dir.resolve("hello.yml");
    Files.writeString(file, VALID.replace("slot: 13", "slot: 40"));
    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");
    assertThrows(InvalidMenuException.class, () -> loader.load(id));

    Files.writeString(file, VALID);
    Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + 5_000));

    MenuConfig recovered = loader.load(id);
    assertEquals(3, recovered.rows());
  }

  @Test
  void cachesPerMenuIdIndependently(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("a.yml"), VALID);
    Files.writeString(dir.resolve("b.yml"), VALID.replace("rows: 3", "rows: 5"));
    MenuLoader loader = new MenuLoader(dir);

    assertEquals(3, loader.load(new MenuId("a")).rows());
    assertEquals(5, loader.load(new MenuId("b")).rows());
    assertSame(loader.load(new MenuId("a")), loader.load(new MenuId("a")));
  }
}
