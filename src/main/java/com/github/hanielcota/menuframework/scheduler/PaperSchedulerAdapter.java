package com.github.hanielcota.menuframework.scheduler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class PaperSchedulerAdapter implements SchedulerAdapter {

  @Override
  public void runSync(@NonNull Plugin plugin, @NonNull Runnable task) {
    Bukkit.getGlobalRegionScheduler()
        .execute(Objects.requireNonNull(plugin, "plugin"), Objects.requireNonNull(task, "task"));
  }

  @Override
  public void runSyncDelayed(@NonNull Plugin plugin, @NonNull Runnable task, long ticks) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(task, "task");
    if (ticks < 0) {
      throw new IllegalArgumentException("ticks must be >= 0, got: " + ticks);
    }

    Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), ticks);
  }

  @Override
  public void runAsync(@NonNull Plugin plugin, @NonNull Runnable task) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(task, "task");
    Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
  }

  @Override
  public void runAsyncDelayed(@NonNull Plugin plugin, @NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(unit, "unit");
    if (delay < 0) {
      throw new IllegalArgumentException("delay must be >= 0, got: " + delay);
    }

    Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), delay, unit);
  }

  @Override
  public @NonNull Object runAsyncRepeating(
      @NonNull Plugin plugin,
      @NonNull Runnable task,
      long delay,
      long period,
      @NonNull TimeUnit unit) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(unit, "unit");
    if (delay < 0) {
      throw new IllegalArgumentException("delay must be >= 0, got: " + delay);
    }
    if (period <= 0) {
      throw new IllegalArgumentException("period must be > 0, got: " + period);
    }
    return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), delay, period, unit);
  }

  @Override
  public @NonNull Object runSyncRepeating(
      @NonNull Plugin plugin, @NonNull Runnable task, long delayTicks, long periodTicks) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(task, "task");
    if (delayTicks < 0) {
      throw new IllegalArgumentException("delayTicks must be >= 0, got: " + delayTicks);
    }
    if (periodTicks <= 0) {
      throw new IllegalArgumentException("periodTicks must be > 0, got: " + periodTicks);
    }
    return Bukkit.getGlobalRegionScheduler()
        .runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks);
  }

  @Override
  public void cancel(@NonNull Object taskHandle) {
    Objects.requireNonNull(taskHandle, "taskHandle");
    if (!(taskHandle instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask task)) {
      java.util.logging.Logger.getLogger(PaperSchedulerAdapter.class.getName())
          .warning("Unexpected task handle type: " + taskHandle.getClass().getName());
      return;
    }
    task.cancel();
  }
}
