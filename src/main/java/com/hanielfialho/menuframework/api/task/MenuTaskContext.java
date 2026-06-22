package com.hanielfialho.menuframework.api.task;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable metadata supplied when asynchronous work starts.
 *
 * <p>The context contains no Bukkit object and may safely cross threads.
 *
 * @param sessionId owning session UUID
 * @param viewerId viewer UUID
 * @param key logical task key
 * @param generation positive task generation
 */
public record MenuTaskContext(UUID sessionId, UUID viewerId, MenuTaskKey key, long generation) {

  /**
   * Validates and creates the context.
   *
   * @throws NullPointerException if an object component is {@code null}
   * @throws IllegalArgumentException if {@code generation} is not positive
   */
  public MenuTaskContext {
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(key, "key");

    if (generation <= 0L) {
      throw new IllegalArgumentException("generation must be greater than zero: " + generation);
    }
  }
}
