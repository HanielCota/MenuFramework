package dev.haniel.menu.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.MenuLoader;
import dev.haniel.menu.config.PaginationConfig;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.merge.PagedMerger;
import dev.haniel.menu.merge.StaticMerger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MenuCompilerTest {

  private static final String STATIC_YAML =
      """
      title: "<green>Static</green>"
      rows: 1
      buttons:
        close:
          slot: 8
          material: BARRIER
          name: "<red>Close</red>"
      """;

  private static final String PAGED_YAML =
      """
      title: "<gold>Paged</gold>"
      rows: 3
      pagination:
        mask:
          - "#########"
          - "#XXXXXXX#"
          - "#########"
      """;

  @Test
  void compilesAnnotatedInstanceOnTheStaticPath(@TempDir Path dir) throws IOException {
    MenuCompiler<String> compiler = compiler(dir);
    write(dir, "static-menu", STATIC_YAML);

    CompiledMenu<String> compiled = compiler.compile(new StaticMenuSample());

    assertInstanceOf(CompiledStaticMenu.class, compiled);
    assertEquals("static-menu", compiled.id().value());
  }

  @Test
  void compilesAnnotatedClassOnThePagedPath(@TempDir Path dir) throws IOException {
    MenuCompiler<String> compiler = compiler(dir);
    write(dir, "paged-menu", PAGED_YAML);

    CompiledMenu<String> compiled = compiler.compile(new PagedMenuSample());

    assertInstanceOf(CompiledPagedMenu.class, compiled);
    assertEquals("paged-menu", compiled.id().value());
  }

  @Test
  void compilesPagedClassWithInstanceFactory(@TempDir Path dir) throws IOException {
    MenuCompiler<String> compiler = compiler(dir);
    write(dir, "paged-menu", PAGED_YAML);

    CompiledMenu<String> compiled =
        compiler.compile(PagedMenuSample.class, type -> new PagedMenuSample());

    assertInstanceOf(CompiledPagedMenu.class, compiled);
  }

  @Test
  void compilesStaticClassWithInstanceFactory(@TempDir Path dir) throws IOException {
    MenuCompiler<String> compiler = compiler(dir);
    write(dir, "static-menu", STATIC_YAML);

    CompiledMenu<String> compiled =
        compiler.compile(StaticMenuSample.class, type -> new StaticMenuSample());

    assertInstanceOf(CompiledStaticMenu.class, compiled);
  }

  @Test
  void compilesStaticInstanceWithPreloadedConfig(@TempDir Path dir) {
    MenuCompiler<String> compiler = compiler(dir);

    CompiledMenu<String> compiled = compiler.compile(new StaticMenuSample(), staticConfig());

    assertInstanceOf(CompiledStaticMenu.class, compiled);
  }

  @Test
  void compilesPagedClassWithFactoryAndPreloadedConfig(@TempDir Path dir) {
    MenuCompiler<String> compiler = compiler(dir);

    CompiledMenu<String> compiled =
        compiler.compile(PagedMenuSample.class, type -> new PagedMenuSample(), pagedConfig());

    assertInstanceOf(CompiledPagedMenu.class, compiled);
  }

  @Test
  void loadsMenuConfigThroughTheStaticLoader(@TempDir Path dir) throws IOException {
    MenuCompiler<String> compiler = compiler(dir);
    write(dir, "static-menu", STATIC_YAML);

    MenuConfig config = compiler.load(new MenuId("static-menu"));

    assertEquals(1, config.rows());
  }

  @Test
  void failsCompilingUnannotatedInstance(@TempDir Path dir) {
    MenuCompiler<String> compiler = compiler(dir);

    assertThrows(InvalidMenuException.class, () -> compiler.compile(new NotAMenu()));
  }

  private static MenuCompiler<String> compiler(Path dir) {
    MenuLoader loader = new MenuLoader(dir);
    StaticCompiler<String> staticCompiler =
        new StaticCompiler<>(new StaticReader(), loader, new StaticMerger<>(Icon::material));
    PagedCompiler<String> pagedCompiler =
        new PagedCompiler<>(new PagedReader(), loader, new PagedMerger<>(Icon::material));
    return new MenuCompiler<>(staticCompiler, pagedCompiler);
  }

  private static MenuConfig staticConfig() {
    ButtonConfig close = new ButtonConfig(8, "BARRIER", "<red>Close</red>", List.of());
    return new MenuConfig("<green>Static</green>", 1, Map.of("close", close), null);
  }

  private static MenuConfig pagedConfig() {
    PaginationConfig pagination =
        new PaginationConfig(List.of("#########", "#XXXXXXX#", "#########"), null, null);
    return new MenuConfig("<gold>Paged</gold>", 3, Map.of(), pagination);
  }

  private static void write(Path dir, String id, String yaml) throws IOException {
    Files.writeString(dir.resolve(id + ".yml"), yaml);
  }

  @Menu(id = "static-menu")
  static final class StaticMenuSample {

    @Button(id = "close")
    void close() {}
  }

  @Menu(id = "paged-menu")
  static final class PagedMenuSample {

    @Paginated
    List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  static final class NotAMenu {}
}
