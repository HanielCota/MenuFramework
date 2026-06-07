package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MenuId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MenuLoaderTest {

  private static final String YAML =
      """
      title: "Hi"
      rows: 3
      buttons:
        buy:
          slot: 13
          material: EMERALD
          name: "<green>Buy</green>"
          lore:
            - "line one"
      """;

  @Test
  void parsesYamlIntoConfig(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("hello.yml"), YAML);
    MenuConfig config = new MenuLoader(dir).load(new MenuId("hello"));
    assertEquals(3, config.rows());
    assertEquals("EMERALD", config.buttons().get("buy").material());
    assertEquals(13, config.buttons().get("buy").slot());
  }

  @Test
  void failsClearlyWhenFileMissing(@TempDir Path dir) {
    assertThrows(InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("absent")));
  }

  private static final String PAGED_YAML =
      """
      title: "Shop"
      rows: 6
      pagination:
        mask:
          - "#########"
          - "#XXXXXXX#"
          - "#XXXXXXX#"
          - "#XXXXXXX#"
          - "#XXXXXXX#"
          - "#<#####>#"
        previous-button:
          slot: 0
          material: ARROW
          name: "<yellow>Prev</yellow>"
        next-button:
          slot: 0
          material: ARROW
          name: "<yellow>Next</yellow>"
      """;

  @Test
  void parsesPaginationSection(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("shop.yml"), PAGED_YAML);
    MenuConfig config = new MenuLoader(dir).load(new MenuId("shop"));
    assertEquals(6, config.paginationConfig().orElseThrow().mask().size());
    assertEquals("ARROW", config.paginationConfig().orElseThrow().nextButton().material());
  }

  @Test
  void reusesParsedConfigWhenFileIsUnchanged(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("hello.yml"), YAML);
    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");

    MenuConfig first = loader.load(id);
    MenuConfig second = loader.load(id);

    assertSame(first, second);
  }

  @Test
  void reparsesConfigWhenFileChanges(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("hello.yml");
    Files.writeString(file, YAML);
    MenuLoader loader = new MenuLoader(dir);
    MenuId id = new MenuId("hello");
    MenuConfig first = loader.load(id);

    String changed = YAML.replace("rows: 3", "rows: 4");
    Files.writeString(file, changed);
    Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + 2_000));

    MenuConfig second = loader.load(id);
    assertNotSame(first, second);
    assertEquals(4, second.rows());
  }

  @Test
  void rejectsButtonSlotOutsideMenuSize(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("bad.yml"), YAML.replace("slot: 13", "slot: 40"));

    assertThrows(InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("bad")));
  }

  @Test
  void rejectsInvalidPaginationMaskAtLoadTime(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("bad.yml"), PAGED_YAML.replace("#XXXXXXX#", "#XXXXXX?#"));

    assertThrows(InvalidMenuException.class, () -> new MenuLoader(dir).load(new MenuId("bad")));
  }
}
