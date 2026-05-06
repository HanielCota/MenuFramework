package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class RenderEngine {

  @NonNull private final RenderStrategy staticStrategy;
  @NonNull private final RenderStrategy paginatedStrategy;

  public RenderEngine(
      @NonNull RenderStrategy staticStrategy, @NonNull RenderStrategy paginatedStrategy) {
    this.staticStrategy = staticStrategy;
    this.paginatedStrategy = paginatedStrategy;
  }

  public @NonNull RenderResult render(
      @NonNull InventoryView view, @NonNull MenuDefinition definition, int targetPage) {
    var topInventory = view.getTopInventory();
    java.util.Objects.requireNonNull(topInventory, "topInventory is null for menu: " + definition.id() + " (view may be closed)");
    var slots = topInventory.getSize();
    var pagination = definition.pagination();
    java.util.Objects.requireNonNull(pagination, "pagination");
    var strategy = pagination.enabled() ? paginatedStrategy : staticStrategy;
    return strategy.render(new RenderRequest(view, definition, targetPage, slots));
  }
}
