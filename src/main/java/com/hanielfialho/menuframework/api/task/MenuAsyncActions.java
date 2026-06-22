package com.hanielfialho.menuframework.api.task;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.bukkit.entity.Player;

/**
 * Exposes asynchronous operations owned by the current menu session.
 *
 * <p>The runtime first executes the start transition and renders its resulting state on the
 * viewer's entity scheduler. It then starts {@link Operation} on Paper's asynchronous scheduler.
 * Completion is returned to the entity scheduler, where {@link Success} or {@link Failure} is
 * applied only when the session, task key and generation are still current.
 *
 * <p>The asynchronous operation receives only a {@link MenuTaskContext}. It must not capture {@link
 * Player}, inventories, worlds or other region-bound Bukkit objects.
 *
 * @param <S> session-state type
 */
public interface MenuAsyncActions<S> {

  /**
   * Returns the identifier of the current session.
   *
   * @return session UUID
   */
  UUID sessionId();

  /**
   * Returns the player who owns the current session.
   *
   * <p>The returned object is valid only while the current synchronous callback is active. It must
   * not be retained by asynchronous work.
   *
   * @return session viewer
   */
  Player viewer();

  /**
   * Returns the state currently visible to this callback.
   *
   * @return non-null state
   */
  S state();

  /**
   * Returns the current session revision.
   *
   * @return positive revision number
   */
  long revision();

  /**
   * Requests an asynchronous operation with explicit state transitions.
   *
   * <p>At most one asynchronous operation may be requested by the same menu interaction or open
   * callback. Transition callbacks run on the viewer's entity scheduler and therefore must be fast,
   * deterministic and return a non-null state.
   *
   * <p>Starting an operation with a key that is already active invalidates the previous generation.
   * Cancellation is best effort; session identity and generation validation are the authoritative
   * stale-result barrier.
   *
   * @param key logical task key
   * @param operation work started outside the region tick
   * @param onStart transition applied before the work starts
   * @param onSuccess transition applied after successful completion
   * @param onFailure transition applied after a recoverable failure
   * @param <R> asynchronous result type
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalStateException if the callback has already requested an asynchronous operation
   *     or is no longer active
   */
  <R> void executeAsync(
      MenuTaskKey key,
      Operation<R> operation,
      Start<S> onStart,
      Success<S, R> onSuccess,
      Failure<S> onFailure);

  /**
   * Work started by Paper's asynchronous scheduler.
   *
   * @param <R> result type
   */
  @FunctionalInterface
  interface Operation<R> {

    /**
     * Starts the asynchronous work.
     *
     * <p>The returned stage and its successful value must both be non-null. The context contains no
     * region-bound Bukkit object and may safely cross threads.
     *
     * @param context immutable task metadata
     * @return non-null stage producing a non-null result
     * @throws Exception if the work cannot be started
     */
    CompletionStage<? extends R> start(MenuTaskContext context) throws Exception;
  }

  /**
   * State transition executed before asynchronous work starts.
   *
   * @param <S> state type
   */
  @FunctionalInterface
  interface Start<S> {

    /**
     * Produces the initial operation state, normally a loading state.
     *
     * @param currentState current non-null state
     * @param generation newly reserved positive generation
     * @return non-null replacement state
     */
    S apply(S currentState, long generation);
  }

  /**
   * State transition executed after successful completion.
   *
   * @param <S> state type
   * @param <R> result type
   */
  @FunctionalInterface
  interface Success<S, R> {

    /**
     * Produces the successful state.
     *
     * @param currentState state current when completion is applied
     * @param generation completed generation
     * @param result non-null operation result
     * @return non-null replacement state
     */
    S apply(S currentState, long generation, R result);
  }

  /**
   * Recovery transition executed after an asynchronous or application failure.
   *
   * <p>This transition may also be invoked when the success transition or its resulting render
   * fails. Fatal JVM errors are never converted into a recoverable menu state.
   *
   * @param <S> state type
   */
  @FunctionalInterface
  interface Failure<S> {

    /**
     * Produces a recoverable state, normally an error state with a retry action.
     *
     * @param currentState state current when the failure is applied
     * @param generation failed generation
     * @param failure observed failure
     * @return non-null replacement state
     */
    S apply(S currentState, long generation, Throwable failure);
  }
}
