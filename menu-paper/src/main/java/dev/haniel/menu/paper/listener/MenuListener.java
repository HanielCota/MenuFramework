package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.holder.ClickableHolder;
import dev.haniel.menu.paper.reactive.ReactiveView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * The single listener that drives every menu.
 *
 * <p>It cancels the click, validates that the inventory belongs to a {@link ClickableHolder}, and
 * delegates by raw slot. It reads {@code getRawSlot()} (not {@code getSlot()}) so bottom- inventory
 * clicks fall outside the menu bounds and resolve to no action. On close it tears down reactive
 * views so no closed view stays referenced. Dragging is cancelled as well; otherwise a player could
 * drag items into the menu without an {@link InventoryClickEvent}.
 */
public final class MenuListener implements Listener {

  /**
   * Handles a click inside an open inventory.
   *
   * @param event the click event
   */
  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof ClickableHolder holder)) {
      return;
    }
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    holder.click(event.getRawSlot(), context(player, event.getClick()));
  }

  /**
   * Blocks drag insertion or extraction while a menu inventory is open.
   *
   * @param event the drag event
   */
  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    if (!(event.getInventory().getHolder() instanceof ClickableHolder)) {
      return;
    }
    event.setCancelled(true);
  }

  /**
   * Tears down a reactive view when its inventory closes.
   *
   * @param event the close event
   */
  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getInventory().getHolder() instanceof ReactiveView view)) {
      return;
    }
    view.close();
  }

  private ClickContext context(Player player, ClickType click) {
    PlayerId playerId = new PlayerId(player.getUniqueId());
    return new PaperClickContext(playerId, ClickTypeMapper.map(click), player);
  }
}
