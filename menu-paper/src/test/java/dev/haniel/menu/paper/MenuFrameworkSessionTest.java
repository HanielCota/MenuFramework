package dev.haniel.menu.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuSession;
import dev.haniel.menu.paper.holder.OpenMenu;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Resolving a player's open menu from the live inventory holder — the framework keeps no per-player
 * view registry, so {@code session} must read the open inventory and recognise only its own
 * holders.
 */
class MenuFrameworkSessionTest {

  @Test
  void sessionWrapsAnOpenFrameworkMenu() {
    Player player = playerViewing(new FakeMenu(new MenuId("shop")));

    Optional<MenuSession> session = framework().session(player);

    assertTrue(session.isPresent(), "an open framework menu must resolve to a session");
    assertEquals("shop", session.orElseThrow().menuId().value());
  }

  @Test
  void sessionIsEmptyForANonFrameworkInventory() {
    Player player = playerViewing(mock(InventoryHolder.class));

    assertTrue(framework().session(player).isEmpty(), "a foreign inventory must not resolve");
  }

  @Test
  void sessionByIdIsEmptyWhenThePlayerIsOffline() {
    UUID id = UUID.randomUUID();
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(id)).thenReturn(null);

      assertTrue(framework().session(new PlayerId(id)).isEmpty());
    }
  }

  private static MenuFramework framework() {
    // session() reads only the player; the registry, scanner and lifecycle are unused here.
    return new MenuFramework(null, null, null);
  }

  private static Player playerViewing(InventoryHolder holder) {
    Player player = mock(Player.class);
    InventoryView view = mock(InventoryView.class);
    Inventory inventory = mock(Inventory.class);
    when(player.getOpenInventory()).thenReturn(view);
    when(view.getTopInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(holder);
    return player;
  }

  private record FakeMenu(MenuId id) implements OpenMenu {
    @Override
    public MenuId menuId() {
      return id;
    }

    @Override
    public void refresh() {
      // no-op: session resolution does not re-render
    }

    @Override
    public Inventory getInventory() {
      return null;
    }
  }
}
