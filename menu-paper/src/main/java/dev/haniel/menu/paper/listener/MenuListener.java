package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.holder.ClickableHolder;
import dev.haniel.menu.paper.reactive.ReactiveView;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

/**
 * The single listener that drives every menu.
 *
 * <p>It cancels the click, validates that the inventory belongs to a {@link ClickableHolder}, and
 * delegates by raw slot. It reads {@code getRawSlot()} (not {@code getSlot()}) so bottom- inventory
 * clicks fall outside the menu bounds and resolve to no action. On close it tears down reactive
 * views so no closed view stays referenced. Dragging and automated item movement are cancelled as
 * well; otherwise a player (or a hopper) could move items into or out of the menu without an {@link
 * InventoryClickEvent}.
 *
 * <p>Click and drag run at {@link EventPriority#HIGHEST} so the cancel is the last word and another
 * plugin cannot silently re-enable the item move. A button action that throws is caught and logged,
 * not allowed to escape into Bukkit's event pipeline (the cancel is already committed first).
 */
public final class MenuListener implements Listener {

  private final Logger logger;

  /** Creates a listener logging to the framework's default logger. */
  public MenuListener() {
    this(Logger.getLogger(MenuListener.class.getName()));
  }

  /**
   * Creates a listener logging to the given logger.
   *
   * @param logger the logger for failed button actions; never null
   */
  public MenuListener(Logger logger) {
    this.logger = logger;
  }

  /**
   * Handles a click inside an open inventory.
   *
   * @param event the click event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onClick(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof ClickableHolder holder)) {
      return;
    }
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    dispatch(holder, event.getRawSlot(), player, event.getClick());
  }

  private void dispatch(ClickableHolder holder, int rawSlot, Player player, ClickType click) {
    try {
      holder.click(rawSlot, context(player, click));
    } catch (RuntimeException failure) {
      logger.warning("Menu action failed for " + player.getName() + ": " + failure);
    }
  }

  /**
   * Blocks drag insertion or extraction while a menu inventory is open.
   *
   * @param event the drag event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDrag(InventoryDragEvent event) {
    if (!(event.getInventory().getHolder() instanceof ClickableHolder)) {
      return;
    }
    event.setCancelled(true);
  }

  /**
   * Blocks automated item movement (hoppers, droppers) touching a menu inventory.
   *
   * @param event the move event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMove(InventoryMoveItemEvent event) {
    if (touchesMenu(event.getSource()) || touchesMenu(event.getDestination())) {
      event.setCancelled(true);
    }
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

  /**
   * Tears down a reactive view left open by a player who disconnected, the backstop for platforms
   * or crashes that do not fire {@link InventoryCloseEvent} on quit.
   *
   * @param event the quit event
   */
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    if (event.getPlayer().getOpenInventory().getTopInventory().getHolder()
        instanceof ReactiveView view) {
      view.close();
    }
  }

  private boolean touchesMenu(Inventory inventory) {
    return inventory.getHolder() instanceof ClickableHolder;
  }

  private ClickContext context(Player player, ClickType click) {
    return new PaperClickContext(new PlayerId(player.getUniqueId()), ClickTypeMapper.map(click));
  }
}
