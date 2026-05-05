package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record PageBuildRequest(
    @NonNull MenuDefinition definition,
    @NonNull List<SlotDefinition> dynamicItems,
    int requestedPage,
    int slotCount) {

  public PageBuildRequest {
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(dynamicItems, "dynamicItems");
    if (requestedPage < 0) {
      throw new IllegalArgumentException("requestedPage cannot be negative: " + requestedPage);
    }
    if (slotCount <= 0) {
      throw new IllegalArgumentException("slotCount must be > 0, got: " + slotCount);
    }
  }
}
