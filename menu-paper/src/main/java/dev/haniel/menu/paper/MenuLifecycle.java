package dev.haniel.menu.paper;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class MenuLifecycle {

  private final Plugin plugin;
  private final Listener listener;
  private final Executor syncExecutor;
  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
  private boolean shutdown;

  MenuLifecycle(Plugin plugin, Listener listener, Executor syncExecutor) {
    this.plugin = plugin;
    this.listener = listener;
    this.syncExecutor = syncExecutor;
  }

  Executor asyncExecutor() {
    return ioExecutor;
  }

  Executor syncExecutor() {
    return syncExecutor;
  }

  void shutdown() {
    if (shutdown) {
      return;
    }
    HandlerList.unregisterAll(listener);
    cancelPluginTasks();
    ioExecutor.shutdownNow();
    shutdown = true;
  }

  private void cancelPluginTasks() {
    try {
      Bukkit.getScheduler().cancelTasks(plugin);
    } catch (UnsupportedOperationException foliaHasNoGlobalScheduler) {
      // Folia: re-renders run on per-entity schedulers (not the legacy scheduler) and are torn down
      // with their views via InventoryCloseEvent, so there is nothing to cancel here.
    }
  }
}
