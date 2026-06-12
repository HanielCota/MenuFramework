package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuErrorHandler;
import dev.haniel.menu.paper.holder.ClickableHolder;
import dev.haniel.menu.paper.holder.ConfirmHolder;
import dev.haniel.menu.paper.reactive.ReactiveView;
import java.util.Objects;
import java.util.logging.Level;
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
import org.bukkit.inventory.InventoryHolder;

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
 * plugin cannot silently re-enable the item move. A button action that throws is caught and routed
 * to the {@link MenuErrorHandler} (logged with its stacktrace by default), not allowed to escape
 * into Bukkit's event pipeline (the cancel is already committed first).
 */
public final class MenuListener implements Listener {

  private final MenuErrorHandler errorHandler;
  private final Logger logger;

  /** Creates a listener that logs failed actions to the framework's default logger. */
  public MenuListener() {
    this(Logger.getLogger(MenuListener.class.getName()));
  }

  /**
   * Creates a listener that logs failed actions (with their stacktrace) to the given logger.
   *
   * @param logger the logger for failed button actions; never null
   */
  public MenuListener(Logger logger) {
    this(loggingHandler(logger), logger);
  }

  /**
   * Creates a listener that routes failed actions to the given handler.
   *
   * @param errorHandler the handler invoked when an action throws; never null
   * @param logger the logger used if the handler itself throws; never null
   */
  public MenuListener(MenuErrorHandler errorHandler, Logger logger) {
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  private static MenuErrorHandler loggingHandler(Logger logger) {
    Objects.requireNonNull(logger, "logger");
    return (viewer, failure) ->
        logger.log(Level.WARNING, "Menu action failed for " + viewer.getName(), failure);
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
      reportFailure(player, failure);
    }
  }

  private void reportFailure(Player player, RuntimeException failure) {
    try {
      errorHandler.onError(player, failure);
    } catch (RuntimeException thrownByHandler) {
      logger.log(Level.SEVERE, "Menu error handler threw for " + player.getName(), thrownByHandler);
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
    InventoryHolder holder = event.getInventory().getHolder();
    if (holder instanceof ConfirmHolder confirm) {
      confirm.dismissed();
      return;
    }
    if (holder instanceof ReactiveView view) {
      view.close();
    }
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
