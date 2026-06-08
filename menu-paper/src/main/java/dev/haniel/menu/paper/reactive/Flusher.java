package dev.haniel.menu.paper.reactive;

import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import java.util.logging.Logger;

/**
 * Coalesces re-renders: many marks before the next tick schedule a single flush.
 *
 * <p>Scheduling is delegated to a {@link PlayerScheduler}, so the same coalescing logic works
 * unchanged on Paper (global main thread) and Folia (the player's region thread). While a flush is
 * already scheduled, further marks are no-ops, so N state changes in one tick cause exactly one
 * re-render.
 *
 * <p><strong>Threading:</strong> not thread-safe. {@link #mark()}, {@link #cancel()} and the
 * scheduled {@code run} all touch the pending-task field and must run on the view's owning thread
 * (main on Paper, the player's region on Folia), which is where state changes originate.
 */
public final class Flusher {

  private final PlayerScheduler scheduler;
  private final Runnable flush;
  private final Logger logger;
  private volatile ScheduledTask task;

  /**
   * Creates a flusher that runs the given action on the player's context.
   *
   * @param scheduler the player-scoped scheduler; never null
   * @param flush the re-render action; never null
   */
  public Flusher(PlayerScheduler scheduler, Runnable flush, Logger logger) {
    this.scheduler = scheduler;
    this.flush = flush;
    this.logger = logger;
  }

  /**
   * Schedules a flush for the next tick, unless one is already pending.
   *
   * <p>A scheduler that refuses the work — Folia returning an unscheduled task, or Paper throwing
   * because the plugin is disabling — leaves {@code task} null so the mark can be retried and never
   * lets the rejection escape into the state write that triggered it.
   */
  public void mark() {
    if (task != null) {
      logger.fine("coalesced flush already pending");
      return;
    }
    task = trySchedule();
  }

  private ScheduledTask trySchedule() {
    try {
      ScheduledTask scheduled = scheduler.schedule(this::run);
      if (scheduled.scheduled()) {
        logger.fine("scheduled coalesced flush");
        return scheduled;
      }
      logger.fine("coalesced flush was not accepted by scheduler");
      return null;
    } catch (RuntimeException notSchedulable) {
      logger.fine("scheduler rejected coalesced flush: " + notSchedulable);
      return null;
    }
  }

  /** Cancels any pending flush, e.g. when the view closes. */
  public void cancel() {
    if (task == null) {
      return;
    }
    logger.fine("cancelled pending coalesced flush");
    task.cancel();
    task = null;
  }

  private void run() {
    task = null;
    logger.fine("running coalesced flush");
    flush.run();
  }
}
