package dev.haniel.menu.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/** A {@link ScheduledTask} backed by a Folia region task. */
public final class FoliaScheduledTask implements dev.haniel.menu.scheduler.ScheduledTask {

  private final ScheduledTask task;

  /**
   * Wraps a Folia scheduled task.
   *
   * @param task the Folia task; never null
   */
  public FoliaScheduledTask(ScheduledTask task) {
    this.task = task;
  }

  @Override
  public void cancel() {
    task.cancel();
  }
}
