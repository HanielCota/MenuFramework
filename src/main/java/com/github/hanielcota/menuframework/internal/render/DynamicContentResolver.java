package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  private static final Logger log =
      Logger.getLogger(DynamicContentResolver.class.getName());

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

  private static @NonNull List<SlotDefinition> sanitizeDynamicContent(List<SlotDefinition> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }
    return items.stream().filter(Objects::nonNull).toList();
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

    var start = System.nanoTime();
    var resolvedPlayer = resolvePlayer(view);
    var session = resolveSession(view);

    if (resolvedPlayer == null || session == null) {
      return dynamicContentRegistry.getDynamicContent(menuId);
    }

    var items = providerOpt.get().provide(resolvedPlayer, session);
    var duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

    slowRenderLogger.logIfSlow(menuId, duration);

    return sanitizeDynamicContent(items);
  }

  private @Nullable Player resolvePlayer(@NonNull InventoryView view) {
    var viewer = view.getPlayer();
    var resolvedOpt = serverAccess.findOnlinePlayer(viewer.getUniqueId());
    if (resolvedOpt.isEmpty()) {
      log.log(
          Level.FINE,
          "menu.player.resolved_offline playerUuid={0}",
          viewer.getUniqueId());
      return null;
    }
    return resolvedOpt.get();
  }

  private @Nullable MenuSession resolveSession(@NonNull InventoryView view) {
    var viewer = view.getPlayer();
    if (viewer == null) return null;
    var sessionOpt = menuService.getSession(viewer.getUniqueId());
    return sessionOpt.isPresent() ? sessionOpt.get() : null;
  }
}
