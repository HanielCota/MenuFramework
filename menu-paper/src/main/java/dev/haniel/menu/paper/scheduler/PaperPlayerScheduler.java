package dev.haniel.menu.paper.scheduler;

import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Paper scheduling: the global main thread. The player context is implicit, since Paper has a
 * single main thread that owns every inventory.
 */
public final class PaperPlayerScheduler implements PlayerScheduler {

  private final Plugin plugin;

  /**
   * Creates a scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never null
   */
  public PaperPlayerScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public ScheduledTask schedule(Runnable task) {
    return new PaperScheduledTask(Bukkit.getScheduler().runTask(plugin, task));
  }

  @Override
  public ScheduledTask scheduleRepeating(Runnable task, long period) {
    return new PaperScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, task, period, period));
  }
}
