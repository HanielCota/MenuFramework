package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.MenuRegistry;
import com.github.hanielcota.menuframework.pagination.PaginationEngine;
import org.jspecify.annotations.NonNull;

public final class RenderEngineFactory {

  @NonNull private final MenuRegistry menuRegistry;
  @NonNull private final PaginationEngine paginationEngine;
  @NonNull private final SlotRenderer slotRenderer;
  @NonNull private final ItemStackFactory itemStackFactory;
  @NonNull private final MenuFrameworkConfig config;
  @NonNull private final ServerAccess serverAccess;
  @NonNull private final MenuService menuService;

  public RenderEngineFactory(
      @NonNull MenuRegistry menuRegistry,
      @NonNull PaginationEngine paginationEngine,
      @NonNull SlotRenderer slotRenderer,
      @NonNull ItemStackFactory itemStackFactory,
      @NonNull MenuFrameworkConfig config,
      @NonNull ServerAccess serverAccess,
      @NonNull MenuService menuService) {
    this.menuRegistry = menuRegistry;
    this.paginationEngine = paginationEngine;
    this.slotRenderer = slotRenderer;
    this.itemStackFactory = itemStackFactory;
    this.config = config;
    this.serverAccess = serverAccess;
    this.menuService = menuService;
  }

  public @NonNull RenderEngine create() {
    var navigationRenderer = new NavigationRenderer(menuRegistry, itemStackFactory);
    java.util.Objects.requireNonNull(paginationEngine, "paginationEngine is null");
    var slowRenderLogger = new SlowRenderLogger(config);
    var contentResolver = new DynamicContentResolver(menuRegistry, slowRenderLogger, serverAccess, menuService);
    return new RenderEngine(
        new StaticRenderStrategy(slotRenderer),
        new PaginatedRenderStrategy(
            contentResolver, paginationEngine, navigationRenderer, menuRegistry));
  }
}
