package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MenuId;
import org.junit.jupiter.api.Test;

class MenuCatalogTest {

  @Test
  void rejectsDuplicateMenuIds() {
    MenuCatalog catalog = new MenuCatalog();
    MenuId id = new MenuId("same");

    catalog.put(id, new RegisteredMenu(id, new Object(), (player, argument) -> {}));

    assertThrows(
        InvalidMenuException.class,
        () -> catalog.put(id, new RegisteredMenu(id, new Object(), (player, argument) -> {})));
  }
}
