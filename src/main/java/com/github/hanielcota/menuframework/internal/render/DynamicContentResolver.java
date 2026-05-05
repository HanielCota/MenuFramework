package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Resolves dynamic menu content by querying registered providers or fallback static content.
 *
 * <p>Handles player resolution, session context extraction, and performance monitoring for dynamic
 * content loading.
 */
public final class DynamicContentResolver {

  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(DynamicContentResolver.class.getName());

  @NonNull private final DynamicContentRegistry dynamicContentRegistry;
  @NonNull private final SlowRenderLogger slowRenderLogger;
  @NonNull private final ServerAccess serverAccess;
  @NonNull private final MenuService menuService;

  public DynamicContentResolver(
      @NonNull DynamicContentRegistry dynamicContentRegistry,
      @NonNull SlowRenderLogger slowRenderLogger,
      @NonNull ServerAccess serverAccess,
      @NonNull MenuService menuService) {
    this.dynamicContentRegistry = dynamicContentRegistry;
    this.slowRenderLogger = slowRenderLogger;
    this.serverAccess = serverAccess;
    this.menuService = menuService;
  }

  /**
   * Resolves dynamic content for the given render request.
   *
   * @param view the inventory view to resolve content for
   * @param menuId the menu identifier
   * @return the resolved slot definitions, or empty list if none found
   */
  @NonNull List<SlotDefinition> resolve(@NonNull InventoryView view, @NonNull String menuId) {
    var providerOpt = dynamicContentRegistry.getDynamicContentProvider(menuId);

    if (providerOpt.isEmpty()) {
      return dynamicContentRegistry.getDynamicContent(menuId);
    }

    var start = System.currentTimeMillis();
    var resolvedPlayer = resolvePlayer(view);
    var session = resolveSession(view);

    if (resolvedPlayer == null || session == null) {
      return dynamicContentRegistry.getDynamicContent(menuId);
    }

    var items = providerOpt.get().provide(resolvedPlayer, session);
    var duration = System.currentTimeMillis() - start;

    slowRenderLogger.logIfSlow(menuId, duration);

    return items != null ? items : List.of();
  }

  private @Nullable Player resolvePlayer(@NonNull InventoryView view) {
    var viewer = view.getPlayer();
    var resolved = serverAccess.findOnlinePlayer(viewer.getUniqueId()).orElse(null);
    if (resolved == null) {
      log.log(java.util.logging.Level.FINE, "menu.player.resolved_offline playerUuid={0}", viewer.getUniqueId());
    }
    return resolved;
  }

  private @Nullable MenuSession resolveSession(@NonNull InventoryView view) {
    var viewer = view.getPlayer();
    if (viewer == null) return null;
    return menuService.getSession(viewer.getUniqueId()).orElse(null);
  }
}
