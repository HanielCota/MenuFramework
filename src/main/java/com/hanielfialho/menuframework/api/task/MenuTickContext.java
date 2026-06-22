package com.hanielfialho.menuframework.api.task;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Immutable snapshot of one periodic-task execution.
 *
 * <p>The callback receiving this context runs on the viewer's entity scheduler. The {@link Player}
 * reference must not be retained by asynchronous work.
 *
 * @param sessionId owning session UUID
 * @param viewer session viewer
 * @param state current non-null state
 * @param revision positive current revision
 * @param key logical task key
 * @param generation positive task generation
 * @param execution one-based execution counter
 * @param <S> state type
 */
public record MenuTickContext<S>(
    UUID sessionId,
    Player viewer,
    S state,
    long revision,
    MenuTaskKey key,
    long generation,
    long execution) {

  /**
   * Validates and creates the execution snapshot.
   *
   * @throws NullPointerException if an object component is {@code null}
   * @throws IllegalArgumentException if {@code revision}, {@code generation} or {@code execution}
   *     is not positive
   */
  public MenuTickContext {
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(key, "key");

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }

    if (generation <= 0L) {
      throw new IllegalArgumentException("generation must be greater than zero: " + generation);
    }

    if (execution <= 0L) {
      throw new IllegalArgumentException("execution must be greater than zero: " + execution);
    }
  }
}
