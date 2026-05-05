package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.MenuRegistry;
import com.github.hanielcota.menuframework.pagination.PaginationEngineFactory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class RenderEngineFactory {

  @NonNull private final MenuRegistry menuRegistry;
  @NonNull private final PaginationEngineFactory paginationEngineFactory;
  @NonNull private final SlotRenderer slotRenderer;
  @NonNull private final ItemStackFactory itemStackFactory;
  @NonNull private final MenuFrameworkConfig config;

  public @NonNull RenderEngine create() {
    var navigationRenderer = new NavigationRenderer(menuRegistry, itemStackFactory);
    var paginationEngine = paginationEngineFactory.create();
    java.util.Objects.requireNonNull(paginationEngine, "paginationEngineFactory.create() returned null");
    var slowRenderLogger = new SlowRenderLogger(config);
    var contentResolver = new DynamicContentResolver(menuRegistry, slowRenderLogger);
    return new RenderEngine(
        new StaticRenderStrategy(slotRenderer),
        new PaginatedRenderStrategy(
            contentResolver, paginationEngine, navigationRenderer, menuRegistry));
  }
}
