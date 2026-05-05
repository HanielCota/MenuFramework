package com.github.hanielcota.menuframework.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

/**
 * Functional interface for handling clicks in the player's bottom inventory.
 *
 * <p>Only called when {@code allowPlayerInventoryClicks} is enabled for the menu.
 */
@FunctionalInterface
public interface PlayerInventoryClickHandler {

  /**
   * Called when a player clicks their own inventory while a menu is open.
   *
   * @param player the player who clicked
   * @param clickType the type of click
   * @param slot the slot in the player's inventory (0-based)
   * @param session the active menu session
   */
  void onClick(
      @NonNull Player player,
      @NonNull ClickType clickType,
      int slot,
      @NonNull MenuSession session);
}
