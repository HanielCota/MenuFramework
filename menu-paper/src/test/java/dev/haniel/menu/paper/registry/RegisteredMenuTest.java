package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.domain.MenuId;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class RegisteredMenuTest {

  @Menu(id = "open")
  private static final class PublicMenu {}

  @Menu(id = "vault", permission = "menu.vault")
  private static class GuardedMenu {}

  private static final class InheritedMenu extends GuardedMenu {}

  @Test
  void unrestrictedMenuOpensForAnyone() {
    RegisteredMenu menu = new RegisteredMenu(new MenuId("open"), new PublicMenu(), mock());

    assertTrue(menu.mayOpen(mock(Player.class)));
  }

  @Test
  void guardedMenuChecksThePermission() {
    RegisteredMenu menu = new RegisteredMenu(new MenuId("vault"), new GuardedMenu(), mock());
    Player allowed = mock(Player.class);
    Player denied = mock(Player.class);
    when(allowed.hasPermission("menu.vault")).thenReturn(true);

    assertTrue(menu.mayOpen(allowed));
    assertFalse(menu.mayOpen(denied));
  }

  @Test
  void readsPermissionFromASuperclassAnnotation() {
    RegisteredMenu menu = new RegisteredMenu(new MenuId("vault"), new InheritedMenu(), mock());

    assertFalse(menu.mayOpen(mock(Player.class)));
  }

  @Test
  void unannotatedSourceIsUnrestricted() {
    RegisteredMenu menu = new RegisteredMenu(new MenuId("plain"), new Object(), mock());

    assertTrue(menu.mayOpen(mock(Player.class)));
  }
}
