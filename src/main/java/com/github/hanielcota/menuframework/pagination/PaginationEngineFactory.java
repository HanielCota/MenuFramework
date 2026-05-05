package com.github.hanielcota.menuframework.pagination;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.internal.cache.MenuCacheFactory;
import com.github.hanielcota.menuframework.internal.render.SlotRenderer;
import org.jspecify.annotations.NonNull;

public final class PaginationEngineFactory {

  @NonNull private final MenuFrameworkConfig config;
  @NonNull private final SlotRenderer slotRenderer;

  public PaginationEngineFactory(
      @NonNull MenuFrameworkConfig config, @NonNull SlotRenderer slotRenderer) {
    this.config = java.util.Objects.requireNonNull(config, "config");
    this.slotRenderer = java.util.Objects.requireNonNull(slotRenderer, "slotRenderer");
  }

  public @NonNull PaginationEngine create() {
    return new PaginationEngine(MenuCacheFactory.createPageCache(config), slotRenderer);
  }
}
