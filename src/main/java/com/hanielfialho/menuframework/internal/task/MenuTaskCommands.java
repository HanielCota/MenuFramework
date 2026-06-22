package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.task.MenuPeriodicTask;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Deferred buffer of task commands produced by a single callback. Individual periodic task
 * scheduling is not an all-or-nothing transaction.
 */
public final class MenuTaskCommands<S> {

  private final Map<MenuTaskKey, MenuPeriodicCommand<S>> periodicCommands = new LinkedHashMap<>();

  private final Set<MenuTaskKey> cancellations = new LinkedHashSet<>();

  public void repeat(MenuTaskKey key, MenuTaskSchedule schedule, MenuPeriodicTask<S> task) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(schedule, "schedule");
    Objects.requireNonNull(task, "task");

    this.ensureUnused(key);

    this.periodicCommands.put(key, new MenuPeriodicCommand<>(key, schedule, task));
  }

  public void cancel(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");
    this.ensureUnused(key);
    this.cancellations.add(key);
  }

  public boolean isEmpty() {
    return this.periodicCommands.isEmpty() && this.cancellations.isEmpty();
  }

  public boolean contains(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");

    return this.periodicCommands.containsKey(key) || this.cancellations.contains(key);
  }

  public List<MenuPeriodicCommand<S>> periodicCommands() {
    return List.copyOf(this.periodicCommands.values());
  }

  public List<MenuTaskKey> cancellations() {
    return List.copyOf(this.cancellations);
  }

  private void ensureUnused(MenuTaskKey key) {
    if (this.contains(key)) {
      throw new IllegalStateException(
          "A task command has already been requested "
              + "for key '"
              + key.value()
              + "' in this callback");
    }
  }
}
