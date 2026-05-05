package com.github.hanielcota.menuframework.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

/**
 * Context passed to a menu click callback, providing access to the clicking player, session state,
 * and helper methods for common actions.
 *
 * <p>This interface abstracts the Bukkit event details and provides a higher-level API for handling
 * menu interactions.
 */
public interface ClickContext {

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

  /**
   * Sends a chat message to the clicking player.
   *
   * @param message the message component to send
   */
  void reply(@NonNull Component message);

  /**
   * Sends a MiniMessage-formatted chat message to the clicking player.
   *
   * @param miniMessage the MiniMessage string (e.g., "<green>Hello!")
   */
  void reply(@NonNull String miniMessage);

  /** Closes the current menu session. */
  void close();

  /**
   * Opens another registered menu for the same player.
   *
   * @param menuId the ID of the menu to open
   */
  void open(@NonNull String menuId);

  /** Re-renders the current menu page. */
  void refresh();

  /**
   * Changes the current page and refreshes the menu.
   *
   * @param page the zero-based page number
   */
  void setPage(int page);

  /**
   * Returns the current zero-based page number.
   *
   * @return the current page index
   */
  int currentPage();

  /**
   * Returns the plugin that owns the menu service.
   *
   * @return the owning plugin instance
   */
  @NonNull Plugin plugin();
}
