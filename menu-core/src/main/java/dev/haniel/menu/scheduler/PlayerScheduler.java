package dev.haniel.menu.scheduler;

/**
 * Schedules tasks on one player's context for the next tick.
 *
 * <p>On Paper this is the global main thread; on Folia it is the player's region thread (their
 * {@code EntityScheduler}). Either way, the task runs on the thread allowed to touch that player's
 * inventory, which is the threading invariant the reactive re-render relies on.
 */
public interface PlayerScheduler {

  /**
   * Schedules the task to run once on the player's context next tick.
   *
   * @param task the work to run; never null
   * @return a handle to cancel it before it runs
   */
  ScheduledTask schedule(Runnable task);
}
