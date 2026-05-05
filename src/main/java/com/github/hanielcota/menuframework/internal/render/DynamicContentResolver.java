package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
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
@Slf4j
@RequiredArgsConstructor
public final class DynamicContentResolver {

  @NonNull private final DynamicContentRegistry dynamicContentRegistry;
  @NonNull private final SlowRenderLogger slowRenderLogger;

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
    if (viewer == null) return null;

    var resolved = Bukkit.getPlayer(viewer.getUniqueId());
    if (resolved == null) {
      log.debug("menu.player.resolved_offline playerUuid={}", viewer.getUniqueId());
    }
    return resolved;
  }

  private @Nullable MenuSession resolveSession(@NonNull InventoryView view) {
    var topInventory = view.getTopInventory();
    Objects.requireNonNull(topInventory, "topInventory is null (view may be closed)");
    var holder = topInventory.getHolder();
    return holder instanceof MenuSession s ? s : null;
  }
}
