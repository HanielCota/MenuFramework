package com.github.hanielcota.menuframework.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/**
 * Represents an active menu session for a specific player.
 *
 * <p>A session encapsulates the player's inventory view, current page state, and lifecycle
 * management. Sessions are created when a menu is opened and disposed when the menu closes or the
 * player disconnects.
 *
 * <p>All methods are safe to call from any thread unless otherwise noted.
 */
public interface MenuSession {

  /**
   * Returns the UUID of the player viewing this menu.
   *
   * @return the viewer's unique identifier
   */
  @NonNull UUID viewerId();

  /**
   * Returns the registered menu ID associated with this session.
   *
   * @return the menu identifier
   */
  @NonNull String menuId();

  /**
   * Returns the Bukkit inventory view for this session.
   *
   * @return the current inventory view
   */
  @NonNull InventoryView view();

  /**
   * Returns the current zero-based page number.
   *
   * @return the current page index
   */
  int currentPage();

  /**
   * Sets the current page and triggers a refresh.
   *
   * @param page the zero-based page number to switch to
   * @throws IllegalArgumentException if page is negative
   * @throws IllegalStateException if the session has been disposed
   */
  void setPage(int page);

  /** Triggers a re-render of the current menu page. */
  void refresh();

  /** Closes the menu and disposes the session. */
  void close();

  /**
   * Checks whether the given inventory view belongs to this session.
   *
   * @param other the inventory view to compare
   * @return true if the view represents the same top inventory
   */
  boolean isSameView(@NonNull InventoryView other);

  /**
   * Asynchronously disposes this session, cleaning up resources and canceling scheduled tasks.
   *
   * @return a future that completes when disposal is finished
   */
  @NonNull CompletableFuture<Void> dispose();
}
