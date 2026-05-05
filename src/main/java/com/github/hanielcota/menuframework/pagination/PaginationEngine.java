package com.github.hanielcota.menuframework.pagination;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.render.PageBuildRequest;
import com.github.hanielcota.menuframework.internal.render.SlotRenderer;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public final class PaginationEngine {

  @NonNull private final Cache<PageCacheKey, PageView> pageCache;
  private final SlotRenderer slotRenderer;

  public PaginationEngine(
      @NonNull Cache<PageCacheKey, PageView> pageCache, @NonNull SlotRenderer slotRenderer) {
    this.pageCache = java.util.Objects.requireNonNull(pageCache, "pageCache");
    this.slotRenderer = java.util.Objects.requireNonNull(slotRenderer, "slotRenderer");
  }

  public @NonNull PageView getOrBuildPage(
      @NonNull MenuDefinition definition,
      @NonNull List<SlotDefinition> dynamicItems,
      int pageNumber,
      int slots,
      int contentHash) {
    java.util.Objects.requireNonNull(definition, "definition");
    java.util.Objects.requireNonNull(dynamicItems, "dynamicItems");
    if (slots < 0) {
      throw new IllegalArgumentException("slots cannot be negative: " + slots);
    }
    PageCacheKey key = new PageCacheKey(definition.id(), pageNumber, contentHash);
    return pageCache.get(key, k -> buildPageView(definition, dynamicItems, pageNumber, slots));
  }

  private @NonNull PageView buildPageView(
      @NonNull MenuDefinition definition,
      @NonNull List<SlotDefinition> dynamicItems,
      int requestedPage,
      int slots) {
    var pagination = definition.pagination();
    java.util.Objects.requireNonNull(pagination, "pagination");
    var contentSlots = pagination.contentSlots();
    var itemsPerPage = contentSlots.size();
    if (itemsPerPage == 0) {
      return new PageView(0, new ItemStack[slots], 1);
    }
    var totalPages = Math.max(1, (int) Math.ceil((double) dynamicItems.size() / itemsPerPage));
    var clampedPage = Math.clamp(requestedPage, 0, totalPages - 1);

    return new PageView(
        clampedPage,
        slotRenderer.buildPage(new PageBuildRequest(definition, dynamicItems, clampedPage, slots)),
        totalPages);
  }

  public long estimatedSize() {
    return pageCache.estimatedSize();
  }

  public double hitRate() {
    return pageCache.stats().hitRate();
  }

  public void invalidate(@NonNull String menuId) {
    java.util.Objects.requireNonNull(menuId, "menuId");
    pageCache.asMap().keySet().removeIf(key -> key.menuId().equals(menuId));
  }

  public void invalidateAll() {
    pageCache.invalidateAll();
  }
}
