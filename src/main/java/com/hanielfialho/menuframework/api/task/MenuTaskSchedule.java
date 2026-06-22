package com.hanielfialho.menuframework.api.task;

/**
 * Immutable schedule for a periodic session-owned task.
 *
 * <p>Values are expressed in server ticks. The first execution never occurs in the same callback
 * that registered the task.
 *
 * @param initialDelayTicks initial delay of at least one tick
 * @param periodTicks period of at least one tick
 */
public record MenuTaskSchedule(long initialDelayTicks, long periodTicks) {

  /**
   * Validates and creates the schedule.
   *
   * @throws IllegalArgumentException if either value is less than one
   */
  public MenuTaskSchedule {
    if (initialDelayTicks < 1L) {
      throw new IllegalArgumentException("initialDelayTicks must be >= 1: " + initialDelayTicks);
    }

    if (periodTicks < 1L) {
      throw new IllegalArgumentException("periodTicks must be >= 1: " + periodTicks);
    }
  }

  /**
   * Creates an explicit schedule.
   *
   * @param initialDelayTicks initial delay in ticks
   * @param periodTicks period in ticks
   * @return validated schedule
   * @throws IllegalArgumentException if either value is less than one
   */
  public static MenuTaskSchedule of(long initialDelayTicks, long periodTicks) {
    return new MenuTaskSchedule(initialDelayTicks, periodTicks);
  }

  /**
   * Creates a schedule whose first execution occurs after one complete period.
   *
   * @param periodTicks positive period in ticks
   * @return validated schedule
   * @throws IllegalArgumentException if {@code periodTicks} is less than one
   */
  public static MenuTaskSchedule everyTicks(long periodTicks) {
    return new MenuTaskSchedule(periodTicks, periodTicks);
  }

  /**
   * Creates a schedule whose first execution occurs on the next tick.
   *
   * @param periodTicks positive period in ticks
   * @return validated schedule
   * @throws IllegalArgumentException if {@code periodTicks} is less than one
   */
  public static MenuTaskSchedule startingNextTick(long periodTicks) {
    return new MenuTaskSchedule(1L, periodTicks);
  }
}
