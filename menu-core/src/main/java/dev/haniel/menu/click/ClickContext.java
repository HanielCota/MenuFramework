package dev.haniel.menu.click;

import dev.haniel.menu.domain.PlayerId;

/**
 * The minimal context handed to a button action when it fires.
 *
 * <p>It exposes who clicked and how, never the underlying inventory event. The Paper layer provides
 * the implementation; the domain only depends on this contract.
 */
public interface ClickContext {

  /**
   * Returns the player who clicked.
   *
   * @return the clicking player's id; never null
   */
  PlayerId player();

  /**
   * Returns the kind of click performed.
   *
   * @return the click type; never null
   */
  ClickType clickType();

  /**
   * Tells whether the clicking player holds the given permission.
   *
   * <p>The default denies, since the domain has no permission backend; platform implementations
   * override it with the real check. Used to gate permission-guarded button actions.
   *
   * @param permission the permission node to test; never null
   * @return {@code true} if the player holds the permission
   */
  default boolean hasPermission(String permission) {
    return false;
  }
}
