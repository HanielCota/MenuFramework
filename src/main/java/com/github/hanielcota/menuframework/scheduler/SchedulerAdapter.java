package com.github.hanielcota.menuframework.scheduler;

import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public interface SchedulerAdapter {

  void runSync(@NonNull Plugin plugin, @NonNull Runnable task);

  void runSyncDelayed(@NonNull Plugin plugin, @NonNull Runnable task, long ticks);

  void runAsync(@NonNull Plugin plugin, @NonNull Runnable task);

  void runAsyncDelayed(
      @NonNull Plugin plugin, @NonNull Runnable task, long delay, @NonNull TimeUnit unit);

  /**
   * @return A handle that can be passed to {@link #cancel(Object)}.
   */
  @NonNull Object runAsyncRepeating(
      @NonNull Plugin plugin,
      @NonNull Runnable task,
      long delay,
      long period,
      @NonNull TimeUnit unit);

  /**
   * @return A handle that can be passed to {@link #cancel(Object)}.
   */
  @NonNull Object runSyncRepeating(
      @NonNull Plugin plugin, @NonNull Runnable task, long delayTicks, long periodTicks);

  void cancel(@NonNull Object taskHandle);
}
