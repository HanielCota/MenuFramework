package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuHistory;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.core.profile.BukkitPlayerProfileService;
import com.github.hanielcota.menuframework.core.server.BukkitServerAccess;
import com.github.hanielcota.menuframework.interaction.cooldown.CooldownManager;
import com.github.hanielcota.menuframework.interaction.feature.FeatureInvoker;
import com.github.hanielcota.menuframework.interaction.permission.PermissionChecker;
import com.github.hanielcota.menuframework.interaction.permission.PermissionFallbackRenderer;
import com.github.hanielcota.menuframework.interaction.sound.SoundPlayer;
import com.github.hanielcota.menuframework.interaction.toggle.ToggleManager;
import com.github.hanielcota.menuframework.internal.dispatch.ClickDispatcher;
import com.github.hanielcota.menuframework.internal.dispatch.DefaultMenuEventRouter;
import com.github.hanielcota.menuframework.internal.interaction.ClickExecutor;
import com.github.hanielcota.menuframework.internal.item.CachedItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.MenuRegistry;
import com.github.hanielcota.menuframework.internal.registry.SessionRegistry;
import com.github.hanielcota.menuframework.internal.render.RenderEngineFactory;
import com.github.hanielcota.menuframework.internal.render.SlotRenderer;
import com.github.hanielcota.menuframework.internal.session.MenuSessionImplFactory;
import com.github.hanielcota.menuframework.internal.session.RefreshScheduler;
import com.github.hanielcota.menuframework.internal.session.SessionFactory;
import com.github.hanielcota.menuframework.messaging.DefaultMessageService;
import com.github.hanielcota.menuframework.pagination.PaginationEngineFactory;
import org.jspecify.annotations.NonNull;

public final class MenuRuntimeFactory {

  @NonNull private final MenuService menuService;
  @NonNull private final MenuFrameworkConfig config;
  @NonNull private final MenuHistory menuHistory;

  public MenuRuntimeFactory(
      @NonNull MenuService menuService,
      @NonNull MenuFrameworkConfig config,
      @NonNull MenuHistory menuHistory) {
    this.menuService = menuService;
    this.config = config;
    this.menuHistory = menuHistory;
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
    var playerProfileService = new BukkitPlayerProfileService();
    var itemStackFactory = new CachedItemStackFactory(config, playerProfileService);
    var slotRenderer = new SlotRenderer(itemStackFactory);

    var paginationEngineFactory = new PaginationEngineFactory(config, slotRenderer);
    var paginationEngine = paginationEngineFactory.create();
    var menuRegistry = new MenuRegistry(paginationEngine);
    var sessionRegistry = new SessionRegistry(config);

    var renderEngineFactory =
        new RenderEngineFactory(menuRegistry, paginationEngine, slotRenderer, itemStackFactory, config, serverAccess, menuService);
    var renderEngine = renderEngineFactory.create();

    var refreshScheduler = new RefreshScheduler(plugin, scheduler, serverAccess);
    var messageService = new DefaultMessageService();
    var clickExecutor = createClickExecutor(menuService);
    var sessionImplFactory =
        new MenuSessionImplFactory(plugin, scheduler, serverAccess, renderEngine, menuService, clickExecutor, itemStackFactory, menuHistory, messageService);

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

  private @NonNull ClickExecutor createClickExecutor(
      @NonNull MenuService menuService) {
    return new ClickExecutor(
        new CooldownManager(),
        new PermissionChecker(),
        new PermissionFallbackRenderer(menuService),
        new ToggleManager(),
        new SoundPlayer(),
        new FeatureInvoker());
  }
}
