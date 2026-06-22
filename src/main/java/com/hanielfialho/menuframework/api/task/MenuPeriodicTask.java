package com.hanielfialho.menuframework.api.task;

/**
 * Callback for a periodic task owned by a menu session.
 *
 * <p>The callback runs on the viewer's entity scheduler and must not block the owning region
 * thread. Database, HTTP and other potentially slow work should be expressed through {@link
 * MenuAsyncActions} instead.
 *
 * @param <S> session-state type
 */
@FunctionalInterface
public interface MenuPeriodicTask<S> {

  /**
   * Executes one logical task tick.
   *
   * @param context immutable execution snapshot
   * @return non-null execution result
   * @throws Exception to report a recoverable failure and cancel the task
   */
  MenuTickResult<S> tick(MenuTickContext<S> context) throws Exception;
}
