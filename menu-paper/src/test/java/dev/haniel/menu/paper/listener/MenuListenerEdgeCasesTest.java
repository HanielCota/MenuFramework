package dev.haniel.menu.paper.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.paper.holder.ClickableHolder;
import java.util.UUID;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Adversarial probes of the click/drag/close boundary in {@link MenuListener}.
 *
 * <p>Focus: which interactions get cancelled (item-theft protection), what raw slot is forwarded
 * to the holder, and whether non-player or non-menu interactions are correctly ignored.
 */
class MenuListenerEdgeCasesTest {

  // ---- Click cancellation: the item-theft boundary ----

  @Test
  void cancelsClickWhenTopInventoryIsMenu() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, 0, ClickType.LEFT, mock(Player.class));

    new MenuListener().onClick(event);

    verify(event).setCancelled(true);
  }

  @Test
  void doesNotCancelClickWhenTopInventoryIsNotMenu() {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Inventory inventory = mock(Inventory.class);
    when(event.getInventory()).thenReturn(inventory);

    new MenuListener().onClick(event);

    verify(event, never()).setCancelled(true);
  }

  /**
   * A shift-click anywhere in the view while a menu is open MUST be cancelled. The event's top
   * inventory is the menu, so a shift-click that would otherwise sweep items into the menu has to be
   * blocked even though the routed slot resolves to no action.
   */
  @Test
  void cancelsShiftClickWhileMenuOpen() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, 60, ClickType.SHIFT_LEFT, mock(Player.class));

    new MenuListener().onClick(event);

    verify(event).setCancelled(true);
  }

  /** A number-key (hotbar swap) click while a menu is open MUST also be cancelled. */
  @Test
  void cancelsNumberKeyClickWhileMenuOpen() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, 5, ClickType.NUMBER_KEY, mock(Player.class));

    new MenuListener().onClick(event);

    verify(event).setCancelled(true);
  }

  // ---- Slot routing: raw slot must be forwarded verbatim ----

  @Test
  void forwardsRawSlotToHolder() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, 7, ClickType.LEFT, mock(Player.class));

    new MenuListener().onClick(event);

    ArgumentCaptor<Integer> slot = ArgumentCaptor.forClass(Integer.class);
    verify(holder).click(slot.capture(), org.mockito.ArgumentMatchers.any(ClickContext.class));
    assertEquals(7, slot.getValue());
  }

  /**
   * A click outside the menu (bottom inventory) still reaches the holder with the high raw slot, so
   * the holder's bounds check is responsible for resolving it to no action. Verifies the listener
   * does not silently swallow such clicks before delegating.
   */
  @Test
  void forwardsHighRawSlotForBottomInventoryClick() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, 99, ClickType.LEFT, mock(Player.class));

    new MenuListener().onClick(event);

    ArgumentCaptor<Integer> slot = ArgumentCaptor.forClass(Integer.class);
    verify(holder).click(slot.capture(), org.mockito.ArgumentMatchers.any(ClickContext.class));
    assertEquals(99, slot.getValue());
  }

  /**
   * A raw slot of -999 (click outside the window entirely) is forwarded as-is. The listener relies
   * on the holder's negative-slot guard; it must not pre-filter.
   */
  @Test
  void forwardsNegativeRawSlotWhenClickingOutsideWindow() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = clickEvent(holder, -999, ClickType.LEFT, mock(Player.class));

    new MenuListener().onClick(event);

    ArgumentCaptor<Integer> slot = ArgumentCaptor.forClass(Integer.class);
    verify(holder).click(slot.capture(), org.mockito.ArgumentMatchers.any(ClickContext.class));
    assertEquals(-999, slot.getValue());
  }

  // ---- Non-player clicker: must cancel but not route ----

  /**
   * A non-Player clicker (e.g. a command block / fake viewer) must still have the click cancelled
   * for safety, but must not be routed to an action.
   */
  @Test
  void cancelsButDoesNotRouteWhenClickerIsNotPlayer() {
    ClickableHolder holder = mock(ClickableHolder.class);
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Inventory inventory = mock(Inventory.class);
    when(event.getInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(holder);
    when(event.getWhoClicked()).thenReturn(mock(HumanEntity.class));

    new MenuListener().onClick(event);

    verify(event).setCancelled(true);
    verify(holder, never())
        .click(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any());
  }

  // ---- Drag edge cases ----

  /** A drag whose top inventory is a non-menu must not be cancelled (do not block vanilla drags). */
  @Test
  void ignoresDragWhenHolderIsNonClickableHolder() {
    InventoryDragEvent event = mock(InventoryDragEvent.class);
    Inventory inventory = mock(Inventory.class);
    when(event.getInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(mock(org.bukkit.inventory.InventoryHolder.class));

    new MenuListener().onDrag(event);

    verify(event, never()).setCancelled(true);
  }

  // ---- Close: only reactive views are torn down ----

  /** Closing a static (non-reactive) menu must not attempt any teardown. */
  @Test
  void closeIgnoresNonReactiveHolder() {
    InventoryCloseEvent event = mock(InventoryCloseEvent.class);
    Inventory inventory = mock(Inventory.class);
    when(event.getInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(mock(ClickableHolder.class));

    // ClickableHolder is not a ReactiveView; close() must be a no-op with no exception.
    new MenuListener().onClose(event);
  }

  private InventoryClickEvent clickEvent(
      ClickableHolder holder, int rawSlot, ClickType clickType, Player player) {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Inventory inventory = mock(Inventory.class);
    when(event.getInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(holder);
    when(event.getWhoClicked()).thenReturn(player);
    when(event.getRawSlot()).thenReturn(rawSlot);
    when(event.getClick()).thenReturn(clickType);
    when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    return event;
  }
}
