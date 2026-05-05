package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record RenderResult(int resolvedPage, @NonNull Int2ObjectMap<SlotDefinition> slots) {

  public RenderResult {
    Objects.requireNonNull(slots, "slots");
  }
}
