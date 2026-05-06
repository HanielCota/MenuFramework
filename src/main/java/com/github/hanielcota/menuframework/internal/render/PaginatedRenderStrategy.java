package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import com.github.hanielcota.menuframework.pagination.PageView;
import com.github.hanielcota.menuframework.pagination.PaginationEngine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import org.jspecify.annotations.NonNull;

public final class PaginatedRenderStrategy implements RenderStrategy {

  @NonNull private final DynamicContentResolver contentResolver;
  @NonNull private final PaginationEngine paginationEngine;
  @NonNull private final NavigationRenderer navigationRenderer;
  @NonNull private final DynamicContentRegistry dynamicContentRegistry;

  public PaginatedRenderStrategy(
      @NonNull DynamicContentResolver contentResolver,
      @NonNull PaginationEngine paginationEngine,
      @NonNull NavigationRenderer navigationRenderer,
      @NonNull DynamicContentRegistry dynamicContentRegistry) {
    this.contentResolver = contentResolver;
    this.paginationEngine = paginationEngine;
    this.navigationRenderer = navigationRenderer;
    this.dynamicContentRegistry = dynamicContentRegistry;
  }

  private static void collectStaticSlots(
      @NonNull RenderRequest request, @NonNull Int2ObjectMap<SlotDefinition> slots) {
    for (var entry : request.definition().slots().int2ObjectEntrySet()) {
      var slotDef = entry.getValue();
      if (slotDef.handler() != null || slotDef.navigational()) {
        slots.put(entry.getIntKey(), slotDef);
      }
    }
  }

  private static void collectDynamicSlots(
      @NonNull List<SlotDefinition> dynamicItems,
      @NonNull List<Integer> contentSlots,
      @NonNull PageView pageView,
      @NonNull Int2ObjectMap<SlotDefinition> slots) {
    var itemsPerPage = contentSlots.size();
    if (itemsPerPage == 0) return;
    long startLong = (long) pageView.pageNumber() * (long) itemsPerPage;
    if (startLong > Integer.MAX_VALUE || startLong >= dynamicItems.size()) return;
    var start = (int) startLong;
    var end = Math.min(itemsPerPage, dynamicItems.size() - start);

    for (int itemIndex = 0; itemIndex < end; itemIndex++) {
      if (itemIndex >= contentSlots.size()) break;
      var slotDef = dynamicItems.get(start + itemIndex);
      if (slotDef.handler() == null) continue;
      int slot = contentSlots.get(itemIndex);
      if (slot >= 0) {
        slots.put(slot, slotDef);
      }
    }
  }

  @Override
  public @NonNull RenderResult render(@NonNull RenderRequest request) {
    var dynamic = contentResolver.resolve(request.view(), request.definition().id());
    var pageView = resolvePageView(request, dynamic);
    PageApplier.apply(request.view(), pageView, request.slotCount());
    var slots = new Int2ObjectOpenHashMap<SlotDefinition>();
    collectStaticSlots(request, slots);
    collectDynamicSlots(dynamic, request.definition().pagination().contentSlots(), pageView, slots);
    navigationRenderer.render(
        new NavigationRenderContext(
            request.view(),
            request.definition(),
            pageView.pageNumber(),
            pageView.totalPages(),
            slots,
            request.definition().pagination().navigationSlots()));
    return new RenderResult(pageView.pageNumber(), slots);
  }

  private @NonNull PageView resolvePageView(
      @NonNull RenderRequest request, @NonNull List<SlotDefinition> dynamic) {
    var contentHash = dynamicContentRegistry.getDynamicContentHash(request.definition().id());
    return paginationEngine.getOrBuildPage(
        request.definition(), dynamic, request.targetPage(), request.slotCount(), contentHash);
  }
}
