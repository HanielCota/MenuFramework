package dev.haniel.menu.paper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

final class MenuLifecycle {

  private final Plugin plugin;
  private final Listener listener;
  private final Executor syncExecutor;
  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
  private volatile boolean shutdown;

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

  synchronized void shutdown() {
    if (shutdown) {
      return;
    }
    closeOpenMenus();
    HandlerList.unregisterAll(listener);
    cancelPluginTasks();
    ioExecutor.shutdownNow();
    shutdown = true;
  }

  private void closeOpenMenus() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getOpenInventory().getTopInventory().getHolder()
          instanceof dev.haniel.menu.paper.holder.ClickableHolder) {
        player.closeInventory();
      }
    }
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
