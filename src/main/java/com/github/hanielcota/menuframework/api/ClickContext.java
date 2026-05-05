package com.github.hanielcota.menuframework.api;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

/**
 * Context passed to a menu click callback, providing access to the clicking player, session state,
 * and helper methods for common actions.
 *
 * <p>This interface abstracts the Bukkit event details and provides a higher-level API for handling
 * menu interactions.
 */
public interface ClickContext extends NavigationContext, MessagingContext {

  /**
   * Returns the player who clicked the menu.
   *
   * @return the clicking player (guaranteed to be online at the moment of the click)
   */
  @NonNull Player player();

  /**
   * Returns the Adventure audience for the clicking player.
   *
   * @return an audience scoped to the player
   */
  @NonNull Audience audience();

  /**
   * Returns the Bukkit click type that triggered the callback.
   *
   * @return the click type (e.g., LEFT, RIGHT, SHIFT_LEFT)
   */
  @NonNull ClickType clickType();

  /**
   * Returns the active session that received the click.
   *
   * @return the current menu session
   */
  @NonNull MenuSession session();

  /**
   * Returns the raw inventory slot that was clicked.
   *
   * @return the slot index in the top inventory
   */
  int slot();

  /**
   * Returns the raw inventory slot that was clicked. Alias for {@link #slot()}.
   *
   * @return the slot index in the top inventory
   */
  int rawSlot();
}
