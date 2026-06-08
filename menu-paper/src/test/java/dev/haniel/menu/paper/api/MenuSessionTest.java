package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.holder.OpenMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

/** The session handle delegates to the open menu and the viewing player. */
class MenuSessionTest {

  @Test
  void menuIdDelegatesToTheOpenMenu() {
    MenuSession session =
        new MenuSession(mock(Player.class), new RecordingMenu(new MenuId("shop")));

    assertEquals("shop", session.menuId().value());
  }

  @Test
  void refreshDelegatesToTheOpenMenu() {
    RecordingMenu menu = new RecordingMenu(new MenuId("shop"));

    new MenuSession(mock(Player.class), menu).refresh();

    assertEquals(1, menu.refreshes(), "refresh must re-render the open menu exactly once");
  }

  @Test
  void closeClosesThePlayersInventory() {
    Player player = mock(Player.class);

    new MenuSession(player, new RecordingMenu(new MenuId("shop"))).close();

    verify(player).closeInventory();
  }

  private static final class RecordingMenu implements OpenMenu {
    private final MenuId id;
    private int refreshes;

    RecordingMenu(MenuId id) {
      this.id = id;
    }

    @Override
    public MenuId menuId() {
      return id;
    }

    @Override
    public void refresh() {
      refreshes++;
    }

    int refreshes() {
      return refreshes;
    }

    @Override
    public Inventory getInventory() {
      return null;
    }
  }
}
