package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public final class SlotRenderer {

  @NonNull private final ItemStackFactory itemStackFactory;

  public SlotRenderer(@NonNull ItemStackFactory itemStackFactory) {
    this.itemStackFactory = itemStackFactory;
  }

  public void renderStaticSlots(
      @NonNull InventoryView view,
      @NonNull MenuDefinition definition,
      @NonNull Int2ObjectMap<SlotDefinition> slots) {
    for (var entry : definition.slots().int2ObjectEntrySet()) {
      var slot = entry.getValue();
      var template = slot.template();
      if (template != null) {
        view.setItem(entry.getIntKey(), itemStackFactory.create(template));
      }

      if (slot.handler() != null || slot.navigational()) {
        slots.put(entry.getIntKey(), slot);
      }
    }
  }

  public @NonNull ItemStack[] buildPage(@NonNull PageBuildRequest request) {
    var pagination = request.definition().pagination();
    java.util.Objects.requireNonNull(pagination, "pagination");
    var contentSlots = pagination.contentSlots();
    var itemsPerPage = contentSlots.size();
    if (itemsPerPage == 0) {
      return new ItemStack[request.slotCount()];
    }
    var totalPages =
        Math.max(1, (int) Math.ceil((double) request.dynamicItems().size() / itemsPerPage));
    var clampedPage = Math.clamp(request.requestedPage(), 0, totalPages - 1);

    var target = new ItemStack[request.slotCount()];
    renderStaticPageSlots(request.definition(), target);
    fillEmptySlots(request.definition(), target, request.slotCount());
    renderDynamicPageSlots(
        request.dynamicItems(), contentSlots, target, clampedPage, request.slotCount());
    return target;
  }

  private void renderStaticPageSlots(
      @NonNull MenuDefinition definition, @NonNull ItemStack[] target) {
    for (var entry : definition.slots().int2ObjectEntrySet()) {
      var slot = entry.getIntKey();
      if (slot < 0 || slot >= target.length) continue;

      var slotDef = entry.getValue();
      if (slotDef.navigational()) continue;

      var template = slotDef.template();
      if (template != null) {
        target[slot] = itemStackFactory.create(template);
      }
    }
  }

  private void renderDynamicPageSlots(
      @NonNull List<SlotDefinition> items,
      @NonNull List<Integer> contentSlots,
      @NonNull ItemStack[] target,
      int pageNumber,
      int slotCount) {
    int itemsPerPage = contentSlots.size();
    if (itemsPerPage == 0) return;
    long startLong = (long) pageNumber * (long) itemsPerPage;
    if (startLong > Integer.MAX_VALUE || startLong >= items.size()) return;
    int start = (int) startLong;
    int count = Math.min(itemsPerPage, items.size() - start);

    for (int i = 0; i < count; i++) {
      if (i >= contentSlots.size()) break;
      ItemTemplate template = items.get(start + i).template();
      if (template == null) continue;

      int slot = contentSlots.get(i);
      if (slot >= 0 && slot < slotCount) {
        target[slot] = itemStackFactory.create(template);
      }
    }
  }

  private void fillEmptySlots(
      @NonNull MenuDefinition definition, @NonNull ItemStack[] target, int slotCount) {
    var fill = definition.fillItem();
    if (fill == null) return;

    var fillStack = itemStackFactory.create(fill);
    for (int slot = 0; slot < slotCount; slot++) {
      if (target[slot] == null) {
        target[slot] = fillStack;
      }
    }
  }

  public void fillEmptyInventorySlots(
      @NonNull InventoryView view, @NonNull MenuDefinition definition, int slotCount) {
    var fill = definition.fillItem();
    if (fill == null) return;

    var fillStack = itemStackFactory.create(fill);
    for (int slot = 0; slot < slotCount; slot++) {
      if (view.getItem(slot) == null) {
        view.setItem(slot, fillStack);
      }
    }
  }
}
