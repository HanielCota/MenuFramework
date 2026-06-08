package dev.haniel.menu.paper.api;

import org.bukkit.entity.Player;

/**
 * Handles a button action that threw while a menu was open.
 *
 * <p>Registered through {@link dev.haniel.menu.paper.MenuFrameworkBuilder#onActionError}. The click
 * is always cancelled before the handler runs, so the player never keeps a moved item; the handler
 * decides what to do beyond that — message the viewer, report to an error tracker, play a sound.
 * When none is configured, the framework logs the failure (with its full stacktrace) and nothing
 * else.
 *
 * <p>Invoked on the view's owning thread (the main thread on Paper, the player's region thread on
 * Folia), so it may touch the Bukkit API. It must not block on IO. A handler that itself throws
 * does not escape into Bukkit's event pipeline: the failure is logged and swallowed.
 */
@FunctionalInterface
public interface MenuErrorHandler {

  /**
   * Reacts to a failed button action.
   *
   * @param viewer the player whose click failed; never null
   * @param failure the exception thrown by the action; never null
   */
  void onError(Player viewer, RuntimeException failure);
}
