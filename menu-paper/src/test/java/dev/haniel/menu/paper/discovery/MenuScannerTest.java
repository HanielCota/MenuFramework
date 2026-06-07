package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.discovery.DiscoveredMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MenuScannerTest {

  private static final Set<String> ANY_PACKAGE = Set.of("ignored");

  @Test
  void registersGoodMenusAndAggregatesFailuresByClassName() {
    MenuScanner scanner = new MenuScanner(new ClassGraphMenuDiscovery(), new MenuInstantiator());
    List<Object> registered = new ArrayList<>();

    MenuDiscoveryException failure =
        assertThrows(
            MenuDiscoveryException.class,
            () -> scanner.scan(Set.of("dev.haniel.menu.paper.badsamples"), registered::add));

    assertTrue(failure.getMessage().contains("MenuNeedsArg"));
    assertEquals(1, registered.size());
  }

  @Test
  void scanTypesRegistersEveryTypeWithoutInstantiating() {
    MenuScanner scanner =
        new MenuScanner(
            discoveryOf(Alpha.class, Bravo.class),
            type -> {
              throw new IllegalStateException("scanTypes must not instantiate");
            });
    List<Class<?>> registered = new ArrayList<>();

    scanner.scanTypes(ANY_PACKAGE, registered::add);

    assertEquals(2, registered.size());
  }

  @Test
  void scanAggregatesEveryInstantiationFailure() {
    MenuScanner scanner =
        new MenuScanner(
            discoveryOf(Alpha.class, Bravo.class),
            type -> {
              throw new IllegalStateException("boom");
            });

    MenuDiscoveryException failure =
        assertThrows(MenuDiscoveryException.class, () -> scanner.scan(ANY_PACKAGE, menu -> {}));

    assertTrue(failure.getMessage().contains("Alpha"));
    assertTrue(failure.getMessage().contains("Bravo"));
  }

  @Test
  void scanSucceedsWhenNothingIsDiscovered() {
    MenuScanner scanner = new MenuScanner(discoveryOf(), type -> type);
    List<Object> registered = new ArrayList<>();

    scanner.scan(ANY_PACKAGE, registered::add);

    assertTrue(registered.isEmpty());
  }

  private static MenuDiscovery discoveryOf(Class<?>... types) {
    List<DiscoveredMenu> menus = Arrays.stream(types).map(DiscoveredMenu::from).toList();
    return packages -> menus;
  }

  @Menu(id = "alpha")
  static final class Alpha {}

  @Menu(id = "bravo")
  static final class Bravo {}
}
