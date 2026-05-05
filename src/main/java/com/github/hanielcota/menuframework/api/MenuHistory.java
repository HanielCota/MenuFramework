package com.github.hanielcota.menuframework.api;

import java.util.Deque;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Manages navigation history between menus for a player.
 *
 * <p>This allows players to go back to previously opened menus using {@link ClickContext#back()}.
 */
public interface MenuHistory {

  /**
   * Pushes a menu ID onto the player's navigation stack.
   *
   * @param playerUuid the player's UUID
   * @param menuId the menu ID to push
   */
  void push(@NonNull UUID playerUuid, @NonNull String menuId);

  /**
   * Pops the most recent menu from the player's history and returns it.
   *
   * @param playerUuid the player's UUID
   * @return the previous menu ID, if any
   */
  @NonNull Optional<String> pop(@NonNull UUID playerUuid);

  /**
   * Returns the most recent menu without removing it.
   *
   * @param playerUuid the player's UUID
   * @return the previous menu ID, if any
   */
  @NonNull Optional<String> peek(@NonNull UUID playerUuid);

  /**
   * Checks if the player has any previous menus in history.
   *
   * @param playerUuid the player's UUID
   * @return true if there's at least one previous menu
   */
  boolean hasHistory(@NonNull UUID playerUuid);

  /**
   * Clears all history for a player.
   *
   * @param playerUuid the player's UUID
   */
  void clear(@NonNull UUID playerUuid);

  /**
   * Returns a copy of the history stack for inspection.
   *
   * @param playerUuid the player's UUID
   * @return an unmodifiable view of the history
   */
  @NonNull Deque<String> getHistory(@NonNull UUID playerUuid);
}
