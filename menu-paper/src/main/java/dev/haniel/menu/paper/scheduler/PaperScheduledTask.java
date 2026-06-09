package dev.haniel.menu.paper.scheduler;

import dev.haniel.menu.scheduler.ScheduledTask;
import java.util.Objects;
import org.bukkit.scheduler.BukkitTask;

/** A {@link ScheduledTask} backed by a Bukkit task. */
public final class PaperScheduledTask implements ScheduledTask {

  private final BukkitTask task;

  /**
   * Wraps a Bukkit task.
   *
   * @param task the scheduled Bukkit task; never null
   */
  public PaperScheduledTask(BukkitTask task) {
    this.task = Objects.requireNonNull(task, "task");
  }

  @Override
  public void cancel() {
    task.cancel();
  }
}
