package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import com.github.hanielcota.menuframework.pagination.PageView;
import com.github.hanielcota.menuframework.pagination.PaginationEngine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class PaginatedRenderStrategy implements RenderStrategy {

  @NonNull private final DynamicContentResolver contentResolver;
  @NonNull private final PaginationEngine paginationEngine;
  @NonNull private final NavigationRenderer navigationRenderer;
  @NonNull private final DynamicContentRegistry dynamicContentRegistry;

  private static void collectStaticHandlers(
      @NonNull RenderRequest request, @NonNull Int2ObjectMap<ClickHandler> handlers) {
    for (var entry : request.definition().slots().int2ObjectEntrySet()) {
      var handler = entry.getValue().handler();
      if (handler != null) {
        handlers.put(entry.getIntKey(), handler);
      }
    }
  }

  private static void collectDynamicHandlers(
      @NonNull List<SlotDefinition> dynamicItems,
      @NonNull List<Integer> contentSlots,
      @NonNull PageView pageView,
      @NonNull Int2ObjectMap<ClickHandler> handlers) {
    var itemsPerPage = contentSlots.size();
    if (itemsPerPage == 0) return;
    long startLong = (long) pageView.pageNumber() * (long) itemsPerPage;
    if (startLong > Integer.MAX_VALUE || startLong >= dynamicItems.size()) return;
    var start = (int) startLong;
    var count = Math.min(itemsPerPage, dynamicItems.size() - start);

    for (int itemIndex = 0; itemIndex < count; itemIndex++) {
      if (itemIndex >= contentSlots.size()) break;
      var handler = dynamicItems.get(start + itemIndex).handler();
      if (handler == null) continue;
      var slot = contentSlots.get(itemIndex);
      if (slot >= 0) {
        handlers.put(slot, handler);
      }
    }
  }

  @Override
  public @NonNull RenderResult render(@NonNull RenderRequest request) {
    var dynamic = contentResolver.resolve(request.view(), request.definition().id());
    var pageView = resolvePageView(request, dynamic);
    PageApplier.apply(request.view(), pageView, request.slotCount());
    var handlers = new Int2ObjectOpenHashMap<ClickHandler>();
    collectStaticHandlers(request, handlers);
    collectDynamicHandlers(
        dynamic, request.definition().pagination().contentSlots(), pageView, handlers);
    navigationRenderer.render(
        new NavigationRenderContext(
            request.view(),
            request.definition(),
            pageView.pageNumber(),
            pageView.totalPages(),
            handlers,
            request.definition().pagination().navigationSlots()));
    return new RenderResult(pageView.pageNumber(), handlers);
  }

  private @NonNull PageView resolvePageView(
      @NonNull RenderRequest request, @NonNull List<SlotDefinition> dynamic) {
    var contentHash = dynamicContentRegistry.getDynamicContentHash(request.definition().id());
    return paginationEngine.getOrBuildPage(
        request.definition(), dynamic, request.targetPage(), request.slotCount(), contentHash);
  }
}
