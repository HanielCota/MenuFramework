package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;

public final class StaticRenderStrategy implements RenderStrategy {

  @NonNull private final SlotRenderer slotRenderer;

  public StaticRenderStrategy(@NonNull SlotRenderer slotRenderer) {
    this.slotRenderer = slotRenderer;
  }

  @Override
  public @NonNull RenderResult render(@NonNull RenderRequest request) {
    Int2ObjectMap<SlotDefinition> slots = new Int2ObjectOpenHashMap<>();

    slotRenderer.renderStaticSlots(request.view(), request.definition(), slots);
    slotRenderer.fillEmptyInventorySlots(request.view(), request.definition(), request.slotCount());

    return new RenderResult(0, slots);
  }
}
