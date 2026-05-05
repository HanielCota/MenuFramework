package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuMetrics;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.internal.dispatch.MenuEventRouter;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import com.github.hanielcota.menuframework.internal.registry.ItemTemplateRegistry;
import com.github.hanielcota.menuframework.internal.registry.MenuRegistry;
import com.github.hanielcota.menuframework.internal.registry.SessionRegistry;
import com.github.hanielcota.menuframework.internal.session.SessionCommands;
import com.github.hanielcota.menuframework.internal.session.SessionFactory;
import com.github.hanielcota.menuframework.internal.session.SessionQuery;
import com.github.hanielcota.menuframework.pagination.PaginationEngine;
import org.jspecify.annotations.NonNull;

public final class MenuRuntime {

  @NonNull private final PaginationEngine paginationEngine;
  @NonNull private final MenuRegistry menuRegistry;
  @NonNull private final SessionRegistry sessionRegistry;
  @NonNull private final SessionFactory sessionFactory;
  @NonNull private final MenuEventRouter eventRouter;
  @NonNull private final ItemStackFactory itemStackFactory;

  MenuRuntime(
      @NonNull PaginationEngine paginationEngine,
      @NonNull MenuRegistry menuRegistry,
      @NonNull SessionRegistry sessionRegistry,
      @NonNull SessionFactory sessionFactory,
      @NonNull MenuEventRouter eventRouter,
      @NonNull ItemStackFactory itemStackFactory) {
    this.paginationEngine = paginationEngine;
    this.menuRegistry = menuRegistry;
    this.sessionRegistry = sessionRegistry;
    this.sessionFactory = sessionFactory;
    this.eventRouter = eventRouter;
    this.itemStackFactory = itemStackFactory;
  }

  public static @NonNull MenuRuntime create(
      @NonNull MenuService menuService, @NonNull MenuFrameworkConfig config) {
    return new MenuRuntimeFactory(menuService, config).create();
  }

  public @NonNull PaginationEngine paginationEngine() {
    return paginationEngine;
  }

  public @NonNull SessionFactory sessionFactory() {
    return sessionFactory;
  }

  public @NonNull MenuEventRouter eventRouter() {
    return eventRouter;
  }

  public @NonNull MenuRegistry definitions() {
    return menuRegistry;
  }

  public @NonNull ItemTemplateRegistry templates() {
    return menuRegistry;
  }

  public @NonNull DynamicContentRegistry dynamicContent() {
    return menuRegistry;
  }

  public @NonNull SessionQuery sessions() {
    return sessionRegistry;
  }

  public @NonNull SessionCommands sessionCommands() {
    return sessionRegistry;
  }

  public @NonNull MenuMetrics metrics() {
    return new MenuMetrics(
        sessionRegistry.estimatedSessionCount(),
        menuRegistry.estimatedDefinitionCount(),
        sessionRegistry.sessionHitRate(),
        paginationEngine.estimatedSize(),
        paginationEngine.hitRate());
  }

  public void shutdown() {
    sessionRegistry.closeAllSessions();
    menuRegistry.invalidateAll();
    itemStackFactory.clearCache();
  }
}
