package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public record NavigationRenderContext(
    @NonNull InventoryView view,
    @NonNull MenuDefinition definition,
    int currentPage,
    int totalPages,
    @NonNull Int2ObjectMap<ClickHandler> activeHandlers,
    @NonNull List<Integer> navSlots) {

  public NavigationRenderContext {
    Objects.requireNonNull(view, "view");
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(activeHandlers, "activeHandlers");
    Objects.requireNonNull(navSlots, "navSlots");
  }
}
