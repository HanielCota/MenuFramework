package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.task.MenuPeriodicTask;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import java.util.Objects;

/** Comando interno para iniciar uma tarefa periódica. */
public record MenuPeriodicCommand<S>(
    MenuTaskKey key, MenuTaskSchedule schedule, MenuPeriodicTask<S> task) {

  public MenuPeriodicCommand {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(schedule, "schedule");
    Objects.requireNonNull(task, "task");
  }
}
