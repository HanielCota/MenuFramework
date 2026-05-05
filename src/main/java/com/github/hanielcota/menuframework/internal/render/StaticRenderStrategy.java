package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.ClickHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class StaticRenderStrategy implements RenderStrategy {

  @NonNull private final SlotRenderer slotRenderer;

  @Override
  public @NonNull RenderResult render(@NonNull RenderRequest request) {
    Int2ObjectMap<ClickHandler> handlers = new Int2ObjectOpenHashMap<>();

    slotRenderer.renderStaticSlots(request.view(), request.definition(), handlers);
    slotRenderer.fillEmptyInventorySlots(request.view(), request.definition(), request.slotCount());

    return new RenderResult(0, handlers);
  }
}
