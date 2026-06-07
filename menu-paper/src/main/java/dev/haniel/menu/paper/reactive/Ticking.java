package dev.haniel.menu.paper.reactive;

import dev.haniel.menu.compiler.binding.BoundTick;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Drives a view's {@code @Tick} methods through the player's scheduler.
 *
 * <p>{@link #start()} schedules every bound tick on its own period; {@link #cancel()} stops them
 * all, the anti-leak guarantee when the view closes. A tick runs on the view's owning thread, so
 * the method it calls may safely update reactive state.
 *
 * <p><strong>Threading:</strong> not thread-safe. {@code start} and {@code cancel} touch the
 * running handles and must run on the view's owning thread (main on Paper, the player's region on
 * Folia).
 */
public final class Ticking {

  private final PlayerScheduler scheduler;
  private final List<BoundTick> ticks;
  private final List<ScheduledTask> running = new ArrayList<>();

  /**
   * Pairs the player scheduler with the ticks bound for one open view.
   *
   * @param scheduler the owning player's scheduler; never null
   * @param ticks the ticks bound to the per-player instance; never null
   */
  public Ticking(PlayerScheduler scheduler, List<BoundTick> ticks) {
    this.scheduler = scheduler;
    this.ticks = List.copyOf(ticks);
  }

  /** Schedules every tick on its period; a no-op when the view declares none. */
  public void start() {
    ticks.forEach(tick -> running.add(scheduler.scheduleRepeating(tick.callback(), tick.period())));
  }

  /** Cancels every running tick, leaving no scheduled work behind. */
  public void cancel() {
    running.forEach(ScheduledTask::cancel);
    running.clear();
  }
}
