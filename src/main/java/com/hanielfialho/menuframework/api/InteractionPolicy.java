package com.hanielfialho.menuframework.api;

/**
 * Defines which vanilla inventory interactions remain available while a menu session is open.
 *
 * <p>The top inventory always belongs to the framework and remains protected under every policy in
 * this version.
 */
public enum InteractionPolicy {

  /**
   * Cancels clicks and drags across the whole inventory view, including the player's inventory.
   * Known clicks on menu buttons are still dispatched after the underlying inventory event has been
   * cancelled.
   */
  READ_ONLY,

  /**
   * Allows interactions that are confined to the player's inventory.
   *
   * <p>Actions that may reach the top inventory, including shift-click, collect-to-cursor, unknown
   * actions and drags crossing the menu boundary, remain cancelled.
   */
  PLAYER_INVENTORY_ALLOWED
}
