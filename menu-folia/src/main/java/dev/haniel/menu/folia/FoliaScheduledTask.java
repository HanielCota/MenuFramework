package dev.haniel.menu.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;

/** A {@link ScheduledTask} backed by a Folia region task. */
public final class FoliaScheduledTask implements dev.haniel.menu.scheduler.ScheduledTask {

  private final ScheduledTask task;

  /**
   * Wraps a Folia scheduled task.
   *
   * @param task the Folia task; never null
   */
  public FoliaScheduledTask(ScheduledTask task) {
    this.task = Objects.requireNonNull(task, "task");
  }

  @Override
  public void cancel() {
    task.cancel();
  }
}
