package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class MenuRegistryTest {

  @Test
  void reloadReportsSuccessAfterRecompile() {
    MenuId id = new MenuId("alpha");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object menu = new Object();
    CompiledMenu<ItemStack> compiled = compiledWithId(id);
    when(compiler.compile(menu)).thenReturn(compiled);
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(menu);

    ReloadReport report = registry.reloadReport(id);

    assertTrue(report.successful());
    assertEquals(1, report.successCount());
  }

  @Test
  void reloadCapturesCompilerFailureAndKeepsTheOldMenu() {
    MenuId id = new MenuId("beta");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuCatalog catalog = new MenuCatalog();
    MenuRegistry registry = new MenuRegistry(compiler, factory, catalog);
    Object menu = new Object();
    PaperMenu original = mock();
    CompiledMenu<ItemStack> compiled = compiledWithId(id);
    when(compiler.compile(menu)).thenReturn(compiled).thenThrow(new RuntimeException("kaboom"));
    when(factory.create(any())).thenReturn(original);
    registry.register(menu);

    ReloadReport report = registry.reloadReport(id);

    assertFalse(report.successful());
    assertEquals("kaboom", report.failures().get(0).message());
    assertEquals(1, registry.size());
    assertSame(original, catalog.find(id).orElseThrow().current());
  }

  @Test
  void reloadReportForUnknownIdIsEmpty() {
    MenuRegistry registry = new MenuRegistry(mock(), mock(), new MenuCatalog());

    ReloadReport report = registry.reloadReport(new MenuId("ghost"));

    assertTrue(report.successful());
    assertEquals(0, report.successCount());
  }

  @Test
  void reloadAllAggregatesSuccessesAndFailures() {
    MenuId good = new MenuId("good");
    MenuId bad = new MenuId("bad");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object goodMenu = new Object();
    Object badMenu = new Object();
    CompiledMenu<ItemStack> goodCompiled = compiledWithId(good);
    CompiledMenu<ItemStack> badCompiled = compiledWithId(bad);
    when(compiler.compile(goodMenu)).thenReturn(goodCompiled);
    when(compiler.compile(badMenu)).thenReturn(badCompiled).thenThrow(new RuntimeException("boom"));
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(goodMenu);
    registry.register(badMenu);

    ReloadReport report = registry.reloadAllReport();

    assertEquals(1, report.successCount());
    assertEquals(1, report.failures().size());
  }

  private static CompiledMenu<ItemStack> compiledWithId(MenuId id) {
    return new CompiledStaticMenu<>(id, "", null);
  }
}
