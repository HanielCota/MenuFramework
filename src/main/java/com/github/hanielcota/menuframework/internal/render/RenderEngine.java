package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class RenderEngine {

  @NonNull private final RenderStrategy staticStrategy;
  @NonNull private final RenderStrategy paginatedStrategy;

  public @NonNull RenderResult render(
      @NonNull InventoryView view, @NonNull MenuDefinition definition, int targetPage) {
    var topInventory = view.getTopInventory();
    java.util.Objects.requireNonNull(topInventory, "topInventory is null (view may be closed)");
    var slots = topInventory.getSize();
    var pagination = definition.pagination();
    java.util.Objects.requireNonNull(pagination, "pagination");
    var strategy = pagination.enabled() ? paginatedStrategy : staticStrategy;
    return strategy.render(new RenderRequest(view, definition, targetPage, slots));
  }
}
