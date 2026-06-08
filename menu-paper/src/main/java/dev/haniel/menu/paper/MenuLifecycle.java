package dev.haniel.menu.paper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

final class MenuLifecycle {

  private static final boolean FOLIA = detectFolia();

  private final Plugin plugin;
  private final List<Listener> listeners;
  private final Executor syncExecutor;
  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
  private volatile boolean shutdown;

  MenuLifecycle(Plugin plugin, List<Listener> listeners, Executor syncExecutor) {
    this.plugin = plugin;
    this.listeners = List.copyOf(listeners);
    this.syncExecutor = syncExecutor;
  }

  Executor asyncExecutor() {
    return ioExecutor;
  }

  // An in-flight async reload applies its compiled menus here, on the main thread. After shutdown
  // the registry is cleared and Bukkit item-building is no longer safe, so a late apply is rejected
  // instead of run; the reload future then completes via its exceptionally handler.
  Executor syncExecutor() {
    return command -> {
      if (shutdown) {
        throw new RejectedExecutionException("MenuFramework has shut down");
      }
      syncExecutor.execute(command);
    };
  }

  synchronized void shutdown() {
    if (shutdown) {
      return;
    }
    closeOpenMenus();
    listeners.forEach(HandlerList::unregisterAll);
    cancelPluginTasks();
    ioExecutor.shutdownNow();
    shutdown = true;
  }

  private void closeOpenMenus() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (FOLIA) {
        closeOpenMenuOnPlayerThread(player);
      } else {
        closeOpenMenuNow(player);
      }
    }
  }

  private void closeOpenMenuOnPlayerThread(Player player) {
    try {
      player.getScheduler().run(plugin, ignored -> closeOpenMenuNow(player), () -> {});
    } catch (RuntimeException unavailable) {
      // The player or plugin is no longer schedulable; Bukkit will discard the view on disconnect.
    }
  }

  private void closeOpenMenuNow(Player player) {
    if (player.getOpenInventory().getTopInventory().getHolder()
        instanceof dev.haniel.menu.paper.holder.ClickableHolder) {
      player.closeInventory();
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

  private static boolean detectFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException notFolia) {
      return false;
    }
  }
}
