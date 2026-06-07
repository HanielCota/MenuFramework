package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/** Adversarial edge cases for {@link MenuRegistry}: unregistered ids, duplicates, aggregation. */
class MenuRegistryEdgeCasesTest {

  /**
   * {@code reload(id)} javadoc: "returns true if the menu was registered and reloaded". An
   * unregistered id was neither registered nor reloaded, so it must report failure (false).
   */
  @Test
  void reloadOfUnregisteredIdReportsFailureNotSuccess() {
    MenuRegistry registry = new MenuRegistry(mock(), mock(), new MenuCatalog());

    boolean reloaded = registry.reload(new MenuId("ghost"));

    assertFalse(
        reloaded, "reload() must return false for an id that was never registered or reloaded");
  }

  /** reloadReport for an unknown id must not count a phantom success. */
  @Test
  void reloadReportOfUnregisteredIdHasNoSuccessAndDoesNotCompile() {
    MenuCompiler<ItemStack> compiler = mock();
    MenuRegistry registry = new MenuRegistry(compiler, mock(), new MenuCatalog());

    ReloadReport report = registry.reloadReport(new MenuId("ghost"));

    assertEquals(0, report.successCount());
    verify(compiler, never()).compile(any(Object.class));
  }

  /** Opening an unregistered id must be a no-op, never an NPE. */
  @Test
  void openUnregisteredIdIsSilentNoOp() {
    MenuRegistry registry = new MenuRegistry(mock(), mock(), new MenuCatalog());
    Player viewer = mock();

    registry.open(viewer, new MenuId("ghost"));
  }

  /** Opening an unregistered source class must be a no-op, never an NPE. */
  @Test
  void openUnregisteredSourceTypeIsSilentNoOp() {
    MenuRegistry registry = new MenuRegistry(mock(), mock(), new MenuCatalog());
    Player viewer = mock();

    registry.open(viewer, String.class);
  }

  /** Registering two menus that compile to the same id must be rejected, not silently overwrite. */
  @Test
  void registeringDuplicateIdIsRejected() {
    MenuId id = new MenuId("dup");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object first = new Object();
    Object second = new Object();
    when(compiler.compile(first)).thenReturn(compiledWithId(id));
    when(compiler.compile(second)).thenReturn(compiledWithId(id));
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(first);

    assertThrows(InvalidMenuException.class, () -> registry.register(second));
    assertEquals(1, registry.size());
  }

  /** The first registration must survive a rejected duplicate (no partial overwrite). */
  @Test
  void rejectedDuplicateKeepsTheOriginalOpenable() {
    MenuId id = new MenuId("keep");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuCatalog catalog = new MenuCatalog();
    MenuRegistry registry = new MenuRegistry(compiler, factory, catalog);
    Object first = new Object();
    Object second = new Object();
    PaperMenu original = mock();
    PaperMenu replacement = mock();
    when(compiler.compile(first)).thenReturn(compiledWithId(id));
    when(compiler.compile(second)).thenReturn(compiledWithId(id));
    when(factory.create(any())).thenReturn(original).thenReturn(replacement);
    registry.register(first);

    assertThrows(InvalidMenuException.class, () -> registry.register(second));

    assertSame(original, catalog.find(id).orElseThrow().current());
  }

  /** reloadAll over an empty registry reports zero successes and is successful. */
  @Test
  void reloadAllOverEmptyRegistryIsSuccessfulWithZeroCount() {
    MenuRegistry registry = new MenuRegistry(mock(), mock(), new MenuCatalog());

    ReloadReport report = registry.reloadAllReport();

    assertTrue(report.successful());
    assertEquals(0, report.successCount());
    assertEquals(0, registry.reloadAll());
  }

  /** A partial reloadAll must list the exact failing id, not just a count. */
  @Test
  void reloadAllReportsTheExactFailingId() {
    MenuId good = new MenuId("good");
    MenuId bad = new MenuId("bad");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object goodMenu = new Object();
    Object badMenu = new Object();
    when(compiler.compile(goodMenu)).thenReturn(compiledWithId(good));
    when(compiler.compile(badMenu))
        .thenReturn(compiledWithId(bad))
        .thenThrow(new RuntimeException("explode"));
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(goodMenu);
    registry.register(badMenu);

    ReloadReport report = registry.reloadAllReport();

    assertEquals(1, report.successCount());
    assertEquals(bad, report.failures().get(0).id());
    assertTrue(report.reloaded().contains(good));
    assertFalse(report.reloaded().contains(bad));
  }

  /**
   * When the compiler throws an exception with a {@code null} message, the failure must still be
   * aggregated (one failure recorded), even if the message is null.
   */
  @Test
  void reloadAggregatesFailureEvenWhenExceptionMessageIsNull() {
    MenuId id = new MenuId("nullmsg");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object menu = new Object();
    when(compiler.compile(menu))
        .thenReturn(compiledWithId(id))
        .thenThrow(new RuntimeException());
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(menu);

    ReloadReport report = registry.reloadReport(id);

    assertFalse(report.successful());
    assertEquals(1, report.failures().size());
    assertEquals(id, report.failures().get(0).id());
  }

  /** reloadAll must attempt every menu even when an early one fails. */
  @Test
  void reloadAllDoesNotShortCircuitOnFirstFailure() {
    MenuId first = new MenuId("aaa");
    MenuId second = new MenuId("zzz");
    MenuCompiler<ItemStack> compiler = mock();
    MenuFactory factory = mock();
    MenuRegistry registry = new MenuRegistry(compiler, factory, new MenuCatalog());
    Object firstMenu = new Object();
    Object secondMenu = new Object();
    when(compiler.compile(firstMenu))
        .thenReturn(compiledWithId(first))
        .thenThrow(new RuntimeException("boom"));
    when(compiler.compile(secondMenu)).thenReturn(compiledWithId(second));
    when(factory.create(any())).thenReturn(mock(PaperMenu.class));
    registry.register(firstMenu);
    registry.register(secondMenu);

    ReloadReport report = registry.reloadAllReport();

    assertEquals(1, report.successCount());
    assertEquals(1, report.failures().size());
    verify(compiler, times(2)).compile(firstMenu);
    verify(compiler, times(2)).compile(secondMenu);
  }

  private static CompiledMenu<ItemStack> compiledWithId(MenuId id) {
    return new CompiledStaticMenu<>(id, "", null);
  }
}
