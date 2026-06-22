package com.hanielfialho.menuframework.api.task;

import java.util.Objects;

/**
 * Immutable result of one periodic-task execution.
 *
 * <p>When no replacement state is supplied, the current state is preserved. Replacing the state
 * always requests a render. The periodic task stops only when the result carries a stop request.
 *
 * @param <S> session-state type
 */
public final class MenuTickResult<S> {

  private final S nextState;
  private final boolean stateReplacement;
  private final boolean renderRequested;
  private final boolean stopRequested;

  private MenuTickResult(
      S nextState, boolean stateReplacement, boolean renderRequested, boolean stopRequested) {
    this.nextState = stateReplacement ? Objects.requireNonNull(nextState, "nextState") : null;

    this.stateReplacement = stateReplacement;
    this.renderRequested = renderRequested;
    this.stopRequested = stopRequested;
  }

  /**
   * Preserves state and frame, then keeps the task active.
   *
   * @param <S> state type
   * @return continuation result
   */
  public static <S> MenuTickResult<S> continueTask() {
    return new MenuTickResult<>(null, false, false, false);
  }

  /**
   * Preserves the state, requests a render and keeps the task active.
   *
   * @param <S> state type
   * @return refresh result
   */
  public static <S> MenuTickResult<S> refresh() {
    return new MenuTickResult<>(null, false, true, false);
  }

  /**
   * Replaces the state, requests a render and keeps the task active.
   *
   * @param nextState non-null replacement state
   * @param <S> state type
   * @return update result
   * @throws NullPointerException if {@code nextState} is {@code null}
   */
  public static <S> MenuTickResult<S> update(S nextState) {
    return new MenuTickResult<>(nextState, true, true, false);
  }

  /**
   * Stops the task without rendering.
   *
   * @param <S> state type
   * @return stop result
   */
  public static <S> MenuTickResult<S> stop() {
    return new MenuTickResult<>(null, false, false, true);
  }

  /**
   * Renders the current state and then stops the task.
   *
   * @param <S> state type
   * @return stop-and-refresh result
   */
  public static <S> MenuTickResult<S> stopAndRefresh() {
    return new MenuTickResult<>(null, false, true, true);
  }

  /**
   * Replaces the state, renders it and then stops the task.
   *
   * @param nextState non-null replacement state
   * @param <S> state type
   * @return terminal update result
   * @throws NullPointerException if {@code nextState} is {@code null}
   */
  public static <S> MenuTickResult<S> stopWithState(S nextState) {
    return new MenuTickResult<>(nextState, true, true, true);
  }

  /**
   * Returns whether this result requests a render.
   *
   * @return {@code true} when the runtime should render
   */
  public boolean renderRequested() {
    return this.renderRequested;
  }

  /**
   * Returns whether the task should be cancelled after this execution.
   *
   * @return {@code true} when the task should stop
   */
  public boolean stopRequested() {
    return this.stopRequested;
  }

  /**
   * Resolves the state that should be rendered or retained.
   *
   * @param currentState current non-null state
   * @return replacement state, or {@code currentState} when no replacement was requested
   * @throws NullPointerException if {@code currentState} is {@code null}
   */
  public S resolveState(S currentState) {
    Objects.requireNonNull(currentState, "currentState");
    return this.stateReplacement ? this.nextState : currentState;
  }
}
