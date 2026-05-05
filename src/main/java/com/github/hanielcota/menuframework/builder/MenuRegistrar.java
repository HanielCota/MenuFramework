package com.github.hanielcota.menuframework.builder;

import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MenuRegistrar {

  @NonNull private final MenuService menuService;
  @NonNull private final String id;
  @NonNull private final MenuDefinition definition;
  @Nullable private final DynamicContentProvider dynamicContentProvider;
  @NonNull private final List<SlotDefinition> staticDynamicItems;
  private boolean registered = false;

  public MenuRegistrar(
      @NonNull MenuService menuService,
      @NonNull String id,
      @NonNull MenuDefinition definition,
      @Nullable DynamicContentProvider dynamicContentProvider,
      @NonNull List<SlotDefinition> staticDynamicItems) {
    this.menuService = menuService;
    this.id = id;
    this.definition = definition;
    this.dynamicContentProvider = dynamicContentProvider;
    this.staticDynamicItems = staticDynamicItems;
  }

  public synchronized void register() {
    if (registered) {
      throw new IllegalStateException("Menu already registered: " + id);
    }
    registered = true;
    menuService.registerDefinition(definition);

    if (dynamicContentProvider != null) {
      menuService.setDynamicContentProvider(id, dynamicContentProvider);
    } else if (!staticDynamicItems.isEmpty()) {
      menuService.setDynamicContent(id, staticDynamicItems);
    }
  }

  public @NonNull MenuDefinition definition() {
    return definition;
  }
}
