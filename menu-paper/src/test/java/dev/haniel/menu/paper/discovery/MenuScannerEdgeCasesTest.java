package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.discovery.DiscoveredMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Adversarial edge cases for {@link MenuScanner}: duplicates, abstract classes, empty scans. */
class MenuScannerEdgeCasesTest {

  private static final Set<String> ANY_PACKAGE = Set.of("ignored");

  /**
   * When the registrar rejects a duplicate id, the scan must aggregate it as a boot failure naming
   * the class, not let the raw exception escape silently.
   */
  @Test
  void duplicateRegistrationFromRegistrarIsAggregatedAsBootFailure() {
    MenuScanner scanner = new MenuScanner(discoveryOf(Alpha.class, AlphaClone.class), type -> type);
    Set<String> seenIds = new HashSet<>();

    MenuDiscoveryException failure =
        assertThrows(
            MenuDiscoveryException.class,
            () ->
                scanner.scan(
                    ANY_PACKAGE,
                    menu -> {
                      if (!seenIds.add("alpha")) {
                        throw new InvalidMenuException("Duplicate menu id 'alpha'");
                      }
                    }));

    assertTrue(failure.getMessage().contains("Duplicate"));
    assertTrue(failure.getMessage().contains("AlphaClone"));
  }

  /**
   * An abstract @Menu class has no usable constructor; the real {@link MenuInstantiator} must turn
   * that into an aggregated boot failure naming the class, never a leaked InstantiationException.
   */
  @Test
  void abstractMenuClassFailsWithAggregatedClassNamedError() {
    MenuScanner scanner = new MenuScanner(discoveryOf(AbstractMenu.class), new MenuInstantiator());

    MenuDiscoveryException failure =
        assertThrows(MenuDiscoveryException.class, () -> scanner.scan(ANY_PACKAGE, menu -> {}));

    assertTrue(failure.getMessage().contains("AbstractMenu"));
  }

  /** A scan over an empty package set must succeed and register nothing. */
  @Test
  void scanOverEmptyPackageSetRegistersNothing() {
    MenuScanner scanner = new MenuScanner(discoveryOf(), type -> type);
    List<Object> registered = new ArrayList<>();

    scanner.scan(Set.of(), registered::add);

    assertTrue(registered.isEmpty());
  }

  /** A registrar that throws must not stop later good menus from being attempted. */
  @Test
  void oneFailingRegistrarDoesNotBlockTheRest() {
    MenuScanner scanner = new MenuScanner(discoveryOf(Alpha.class, Bravo.class), type -> type);
    List<Class<?>> registered = new ArrayList<>();

    assertThrows(
        MenuDiscoveryException.class,
        () ->
            scanner.scan(
                ANY_PACKAGE,
                menu -> {
                  Class<?> type = (Class<?>) menu;
                  if (type.equals(Alpha.class)) {
                    throw new IllegalStateException("alpha boom");
                  }
                  registered.add(type);
                }));

    assertEquals(List.of(Bravo.class), registered);
  }

  private static MenuDiscovery discoveryOf(Class<?>... types) {
    List<DiscoveredMenu> menus = Arrays.stream(types).map(DiscoveredMenu::from).toList();
    return packages -> menus;
  }

  @Menu(id = "alpha")
  static final class Alpha {}

  @Menu(id = "alpha")
  static final class AlphaClone {}

  @Menu(id = "bravo")
  static final class Bravo {}

  @Menu(id = "abstractmenu")
  abstract static class AbstractMenu {}
}
