package com.hanielfialho.menuframework.api.task;

/**
 * Exposes session-owned task commands to a menu callback.
 *
 * <p>Commands are buffered while the callback is active and are applied only after it returns
 * normally. Keys are processed independently: when several tasks are requested by one callback, a
 * later scheduling rejection does not roll back tasks that were already accepted.
 *
 * @param <S> session-state type
 */
public interface MenuTaskActions<S> {

  /**
   * Starts or replaces a periodic task.
   *
   * <p>Any asynchronous or periodic operation active under the same key is invalidated when the new
   * task is published.
   *
   * @param key logical task key
   * @param schedule validated schedule
   * @param task periodic callback
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalStateException if the surrounding callback is no longer active
   */
  void repeat(MenuTaskKey key, MenuTaskSchedule schedule, MenuPeriodicTask<S> task);

  /**
   * Starts on the next tick and repeats at the supplied period.
   *
   * @param key logical task key
   * @param periodTicks positive period in ticks
   * @param task periodic callback
   * @throws NullPointerException if {@code key} or {@code task} is {@code null}
   * @throws IllegalArgumentException if {@code periodTicks} is less than one
   * @throws IllegalStateException if the surrounding callback is no longer active
   */
  default void repeat(MenuTaskKey key, long periodTicks, MenuPeriodicTask<S> task) {
    this.repeat(key, MenuTaskSchedule.startingNextTick(periodTicks), task);
  }

  /**
   * Requests cancellation of the task active under the supplied key.
   *
   * <p>Cancellation is best effort and occurs after the current callback returns normally. Late
   * results remain protected by session identity and generation validation.
   *
   * @param key key to cancel
   * @throws NullPointerException if {@code key} is {@code null}
   * @throws IllegalStateException if the surrounding callback is no longer active
   */
  void cancelTask(MenuTaskKey key);
}
