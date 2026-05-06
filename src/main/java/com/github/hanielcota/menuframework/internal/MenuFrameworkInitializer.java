package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.internal.dispatch.MenuEventRouter;
import com.github.hanielcota.menuframework.internal.event.MenuListener;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class MenuFrameworkInitializer {

  private MenuFrameworkInitializer() {}

  public static @NonNull DefaultMenuService initialize(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull MenuFrameworkConfig config) {
    if (!plugin.isEnabled()) {
      throw new IllegalStateException("Plugin is not enabled: " + plugin.getName());
    }
    var service = new DefaultMenuService(plugin, scheduler, config);
    registerListener(plugin, scheduler, service.eventRouter(), service);
    return service;
  }

  private static void registerListener(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull MenuEventRouter eventRouter,
      @NonNull MenuService service) {
    var server =
        java.util.Objects.requireNonNull(plugin.getServer(), "plugin.getServer() returned null");
    var pluginManager = server.getPluginManager();
    pluginManager.registerEvents(new MenuListener(plugin, eventRouter, scheduler, service), plugin);
  }
}
