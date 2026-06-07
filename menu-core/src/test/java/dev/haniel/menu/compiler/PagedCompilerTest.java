package dev.haniel.menu.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.MenuLoader;
import dev.haniel.menu.config.PaginationConfig;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.merge.PagedMerger;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PagedCompilerTest {

  @Test
  void handlesOnlyClassesWithPaginatedMethod(@TempDir Path dir) {
    PagedCompiler<String> compiler = compiler(dir);

    assertTrue(compiler.handles(PagedSample.class));
    assertFalse(compiler.handles(NotPaged.class));
  }

  @Test
  void compilesWithPreloadedConfigUsingTheNoArgConstructor(@TempDir Path dir) {
    PagedCompiler<String> compiler = compiler(dir);

    CompiledPagedMenu<String> compiled =
        (CompiledPagedMenu<String>) compiler.compile(PagedSample.class, config());

    assertEquals("paged", compiled.id().value());
  }

  @Test
  void buildsAFreshInstancePerOpenWithCustomInstantiator(@TempDir Path dir) {
    PagedCompiler<String> compiler = compiler(dir);
    Instantiator instantiator = new Instantiator(PagedSample::new);

    CompiledPagedMenu<String> compiled =
        (CompiledPagedMenu<String>) compiler.compile(PagedSample.class, instantiator, config());

    Object first = compiled.wiring().instantiator().create();
    Object second = compiled.wiring().instantiator().create();

    assertNotSame(first, second);
  }

  private static PagedCompiler<String> compiler(Path dir) {
    return new PagedCompiler<>(
        new PagedReader(), new MenuLoader(dir), new PagedMerger<>(Icon::material));
  }

  private static MenuConfig config() {
    PaginationConfig pagination =
        new PaginationConfig(List.of("#########", "#XXXXXXX#", "#########"), null, null);
    return new MenuConfig("Paged", 3, Map.of(), pagination);
  }

  @Menu(id = "paged")
  static final class PagedSample {

    @Paginated
    List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "plain")
  static final class NotPaged {}
}
