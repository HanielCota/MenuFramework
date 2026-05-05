package com.github.hanielcota.menuframework.api;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * A feature that can be attached to a menu to extend its behavior.
 *
 * <p>Features are invoked at specific lifecycle points:
 *
 * <ul>
 *   <li>{@link #onOpen(MenuSession)} — when the menu is first displayed to a player
 *   <li>{@link #onClose(MenuSession)} — when the menu is closed or the session is disposed
 *   <li>{@link #onClick(ClickContext)} — when a player clicks a slot in the menu
 *   <li>{@link #onTick(MenuSession, Player)} — on each refresh tick (if a refresh interval is
 *       configured)
 * </ul>
 *
 * <p>All methods have default no-op implementations, allowing implementations to override only the
 * callbacks they need.
 *
 * @see MenuFeatures
 * @see RefreshingMenuFeature
 */
public interface MenuFeature {

  /**
   * Called when the menu is opened for a player.
   *
   * @param session the newly created menu session
   */
  default void onOpen(@NonNull MenuSession session) {}

  /**
   * Called when the menu is closed or the session is disposed.
   *
   * @param session the menu session being closed
   */
  default void onClose(@NonNull MenuSession session) {}

  /**
   * Called when a player clicks a slot in the menu.
   *
   * @param context the click context with player, slot, and session details
   */
  default void onClick(@NonNull ClickContext context) {}

  /**
   * Called on each refresh tick if a refresh interval is configured via {@link
   * RefreshingMenuFeature}.
   *
   * @param session the active menu session
   * @param viewer the player viewing the menu (guaranteed to be online)
   */
  default void onTick(@NonNull MenuSession session, @NonNull Player viewer) {}
}
