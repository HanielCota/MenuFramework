package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.domain.MenuId;
import java.util.Collection;
import org.junit.jupiter.api.Test;

/** Adversarial edge cases for {@link MenuCatalog}: encapsulation, lookup semantics, mutation. */
class MenuCatalogEdgeCasesTest {

  /**
   * CLAUDE.md rule 9 (first-class collections): {@code all()} must never expose the live internal
   * collection. A caller clearing the returned collection must not wipe the registry.
   */
  @Test
  void allReturnsADefensiveCopyThatCannotMutateTheRegistry() {
    MenuCatalog catalog = new MenuCatalog();
    MenuId id = new MenuId("guarded");
    catalog.put(id, new RegisteredMenu(id, new Object(), (player, argument) -> {}));

    Collection<RegisteredMenu> view = catalog.all();
    try {
      view.clear();
    } catch (UnsupportedOperationException expected) {
      // An immutable view is an acceptable defense too.
    }

    assertEquals(1, catalog.size(), "all() must not expose the mutable internal collection");
    assertTrue(catalog.find(id).isPresent());
  }

  /** find(MenuId) for an absent id must be empty, never throw. */
  @Test
  void findByIdIsEmptyWhenAbsent() {
    MenuCatalog catalog = new MenuCatalog();

    assertTrue(catalog.find(new MenuId("absent")).isEmpty());
  }

  /** find(Class) for an unregistered source type must be empty, never throw. */
  @Test
  void findBySourceTypeIsEmptyWhenAbsent() {
    MenuCatalog catalog = new MenuCatalog();

    assertTrue(catalog.find(String.class).isEmpty());
  }

  /** find(Class) must match by exact registered source class. */
  @Test
  void findBySourceTypeMatchesExactRegisteredClass() {
    MenuCatalog catalog = new MenuCatalog();
    MenuId id = new MenuId("typed");
    SampleSource source = new SampleSource();
    catalog.put(id, new RegisteredMenu(id, source, (player, argument) -> {}));

    assertTrue(catalog.find(SampleSource.class).isPresent());
    assertEquals(id, catalog.find(SampleSource.class).orElseThrow().id());
  }

  /** Duplicate put must be rejected and must not overwrite the first entry. */
  @Test
  void duplicatePutDoesNotOverwriteFirstEntry() {
    MenuCatalog catalog = new MenuCatalog();
    MenuId id = new MenuId("dup");
    RegisteredMenu original = new RegisteredMenu(id, new Object(), (player, argument) -> {});
    RegisteredMenu replacement = new RegisteredMenu(id, new Object(), (player, argument) -> {});
    catalog.put(id, original);

    assertThrows(RuntimeException.class, () -> catalog.put(id, replacement));

    assertEquals(original, catalog.find(id).orElseThrow());
  }

  private static final class SampleSource {}
}
