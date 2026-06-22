package com.hanielfialho.menuframework.internal.inventory;

import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Internal event bridge used by the Bukkit listener.
 *
 * <p>This contract keeps listener callbacks out of the public {@code MenuManager} API.
 */
public interface MenuEventHandler {

  /**
   * Returns whether the holder belongs to the current runtime.
   *
   * @param holder inventory holder
   * @return {@code true} when owned by this runtime
   */
  boolean owns(MenuHolder holder);

  /**
   * Dispatches a click captured from the active top inventory.
   *
   * @param viewer viewer that clicked
   * @param sessionId holder session id
   * @param click immutable click snapshot
   */
  void dispatchClick(Player viewer, UUID sessionId, MenuClick click);

  /**
   * Handles the close event for a menu session.
   *
   * @param viewer session owner
   * @param sessionId holder session id
   * @param reason normalized close reason
   */
  void handleInventoryClose(Player viewer, UUID sessionId, MenuCloseReason reason);

  /**
   * Handles removal of every resource owned by a disconnected player.
   *
   * @param viewer disconnected player
   */
  void handleQuit(Player viewer);
}
