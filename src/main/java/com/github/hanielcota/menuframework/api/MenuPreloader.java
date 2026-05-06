package com.github.hanielcota.menuframework.api;

import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Service for pre-loading menu content asynchronously to reduce latency when opening menus.
 *
 * <p>Pre-loading is especially useful for menus with:
 *
 * <ul>
 *   <li>Dynamic content that requires database/API lookups
 *   <li>Pagination with many items
 *   <li>Complex templates or patterns
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Preload a menu when the server starts
 * service.preloader().preload("shop").thenRun(() -> {
 *     logger.info("Shop menu preloaded");
 * });
 *
 * // Preload for a specific player (resolves dynamic content)
 * service.preloader().preload(player, "leaderboard").thenRun(() -> {
 *     logger.info("Leaderboard preloaded for " + player.getName());
 * });
 * }</pre>
 */
public interface MenuPreloader {

  /**
   * Pre-loads a menu definition and its static content (templates, pagination layouts).
   *
   * <p>This is useful for menus without player-specific dynamic content.
   *
   * @param menuId the menu identifier
   * @return a future completed when pre-loading finishes
   */
  @NonNull CompletableFuture<Void> preload(@NonNull String menuId);

  /**
   * Pre-loads a menu with player-specific dynamic content resolved.
   *
   * <p>This fully resolves dynamic content providers and pre-computes pagination pages.
   *
   * @param player the target player
   * @param menuId the menu identifier
   * @return a future completed when pre-loading finishes
   */
  @NonNull CompletableFuture<Void> preload(@NonNull Player player, @NonNull String menuId);

  /**
   * Pre-loads multiple menus at once.
   *
   * @param menuIds the menu identifiers
   * @return a future completed when all pre-loading operations finish
   */
  @NonNull CompletableFuture<Void> preloadAll(@NonNull String... menuIds);

  /**
   * Invalidates pre-loaded content for a menu, forcing re-computation on next preload or open.
   *
   * @param menuId the menu identifier
   */
  void invalidate(@NonNull String menuId);

  /** Invalidates all pre-loaded content. */
  void invalidateAll();
}
