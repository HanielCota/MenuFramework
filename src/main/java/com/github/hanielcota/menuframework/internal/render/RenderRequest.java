package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Objects;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public record RenderRequest(
    @NonNull InventoryView view,
    @NonNull MenuDefinition definition,
    int targetPage,
    int slotCount) {

  public RenderRequest {
    Objects.requireNonNull(view, "view");
    Objects.requireNonNull(definition, "definition");
    if (targetPage < 0) {
      throw new IllegalArgumentException("targetPage cannot be negative: " + targetPage);
    }
    if (slotCount <= 0) {
      throw new IllegalArgumentException("slotCount must be > 0, got: " + slotCount);
    }
  }
}
