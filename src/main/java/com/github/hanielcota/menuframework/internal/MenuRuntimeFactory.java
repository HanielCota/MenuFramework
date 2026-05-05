package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.internal.dispatch.ClickDispatcher;
import com.github.hanielcota.menuframework.internal.dispatch.DefaultMenuEventRouter;
import com.github.hanielcota.menuframework.internal.interaction.ClickExecutor;
import com.github.hanielcota.menuframework.internal.item.CachedItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.MenuRegistry;
import com.github.hanielcota.menuframework.internal.registry.SessionRegistry;
import com.github.hanielcota.menuframework.internal.render.RenderEngineFactory;
import com.github.hanielcota.menuframework.internal.render.SlotRenderer;
import com.github.hanielcota.menuframework.internal.server.BukkitServerAccess;
import com.github.hanielcota.menuframework.internal.session.MenuSessionImplFactory;
import com.github.hanielcota.menuframework.internal.session.RefreshScheduler;
import com.github.hanielcota.menuframework.internal.session.SessionFactory;
import com.github.hanielcota.menuframework.pagination.PaginationEngineFactory;
import org.jspecify.annotations.NonNull;

public final class MenuRuntimeFactory {

  @NonNull private final MenuService menuService;
  @NonNull private final MenuFrameworkConfig config;

  public MenuRuntimeFactory(
      @NonNull MenuService menuService,
      @NonNull MenuFrameworkConfig config) {
    this.menuService = menuService;
    this.config = config;
  }

  public @NonNull MenuRuntime create() {
    if (!(menuService instanceof DefaultMenuService defaultMenuService)) {
      throw new IllegalArgumentException(
          "MenuService must be an instance of DefaultMenuService, got: " + menuService.getClass().getName());
    }
    var plugin = java.util.Objects.requireNonNull(
        menuService.getPlugin(), "menuService.getPlugin() returned null");
    var scheduler = java.util.Objects.requireNonNull(
        defaultMenuService.getScheduler(), "defaultMenuService.getScheduler() returned null");

    var serverAccess = new BukkitServerAccess();
    var itemStackFactory = new CachedItemStackFactory(config);
    var slotRenderer = new SlotRenderer(itemStackFactory);

    var paginationEngineFactory = new PaginationEngineFactory(config, slotRenderer);
    var menuRegistry = new MenuRegistry(paginationEngineFactory.create());
    var sessionRegistry = new SessionRegistry(config);

    var renderEngineFactory =
        new RenderEngineFactory(menuRegistry, paginationEngineFactory, slotRenderer, itemStackFactory, config);
    var renderEngine = renderEngineFactory.create();

    var refreshScheduler = new RefreshScheduler(plugin, scheduler, serverAccess);
    var clickExecutor = new ClickExecutor(menuService);
    var sessionImplFactory =
        new MenuSessionImplFactory(plugin, scheduler, serverAccess, renderEngine, menuService, clickExecutor);

    var sessionFactory =
        new SessionFactory(
            plugin,
            scheduler,
            serverAccess,
            renderEngine,
            menuService,
            sessionRegistry,
            refreshScheduler,
            sessionImplFactory);

    var clickDispatcher = new ClickDispatcher(sessionRegistry);
    var eventRouter = new DefaultMenuEventRouter(sessionRegistry, clickDispatcher);

    return new MenuRuntime(
        menuRegistry.paginationEngine(),
        menuRegistry,
        sessionRegistry,
        sessionFactory,
        eventRouter,
        itemStackFactory);
  }
}
