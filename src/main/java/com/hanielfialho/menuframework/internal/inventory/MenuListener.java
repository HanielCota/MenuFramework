package com.hanielfialho.menuframework.internal.inventory;

import com.hanielfialho.menuframework.api.MenuClick;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/** Listener único que protege views do runtime e encaminha eventos ao manager. */
public final class MenuListener implements Listener {

  private final MenuEventHandler eventHandler;

  public MenuListener(MenuEventHandler eventHandler) {
    this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler");
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryClick(InventoryClickEvent event) {
    MenuHolder holder = this.findHolder(event.getView());

    if (holder == null) {
      return;
    }

    if (!(event.getWhoClicked() instanceof Player player)) {
      event.setCancelled(true);
      return;
    }

    boolean previouslyCancelled = event.isCancelled();

    InventoryInteractionGuard.ClickDecision decision =
        InventoryInteractionGuard.decideClick(
            holder.interactionPolicy(),
            this.findClickArea(event),
            event.getClick(),
            event.getAction());

    if (decision.cancel()) {
      event.setCancelled(true);
    }

    if (previouslyCancelled || !decision.dispatchButton()) {
      return;
    }

    int rawSlot = event.getRawSlot();
    int topSize = event.getView().getTopInventory().getSize();

    if (rawSlot < 0 || rawSlot >= topSize) {
      return;
    }

    MenuClick click =
        new MenuClick(rawSlot, event.getClick(), event.getAction(), event.getHotbarButton());

    this.eventHandler.dispatchClick(player, holder.sessionId(), click);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryDrag(InventoryDragEvent event) {
    MenuHolder holder = this.findHolder(event.getView());

    if (holder == null) {
      return;
    }

    if (!(event.getWhoClicked() instanceof Player)) {
      event.setCancelled(true);
      return;
    }

    int topSize = event.getView().getTopInventory().getSize();

    if (InventoryInteractionGuard.shouldCancelDrag(
        holder.interactionPolicy(), topSize, event.getRawSlots())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClose(InventoryCloseEvent event) {
    MenuHolder holder = this.findHolder(event.getView());

    if (holder == null || !(event.getPlayer() instanceof Player player)) {
      return;
    }

    this.eventHandler.handleInventoryClose(
        player, holder.sessionId(), MenuCloseReasonMapper.map(event.getReason()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.eventHandler.handleQuit(event.getPlayer());
  }

  private InventoryInteractionGuard.ClickArea findClickArea(InventoryClickEvent event) {
    Inventory clickedInventory = event.getClickedInventory();

    if (clickedInventory == null) {
      return InventoryInteractionGuard.ClickArea.OUTSIDE;
    }

    InventoryView view = event.getView();

    if (clickedInventory == view.getTopInventory()) {
      return InventoryInteractionGuard.ClickArea.TOP;
    }

    if (clickedInventory == view.getBottomInventory()) {
      return InventoryInteractionGuard.ClickArea.PLAYER;
    }

    return InventoryInteractionGuard.ClickArea.UNKNOWN;
  }

  private MenuHolder findHolder(InventoryView view) {
    Inventory topInventory = view.getTopInventory();

    MenuHolder holder = MenuViewAccess.holderOf(topInventory);

    if (holder == null || !this.eventHandler.owns(holder)) {
      return null;
    }

    return holder;
  }
}
