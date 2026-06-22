package com.hanielfialho.menuframework.api.pagination.async;

import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable metadata for one asynchronous page request.
 *
 * <p>The context contains no Bukkit objects and is safe to pass into database or HTTP layers.
 *
 * @param sessionId session that initiated the load
 * @param viewerId viewer UUID
 * @param taskKey logical paginator key
 * @param generation positive request generation
 */
public record PageLoadContext(UUID sessionId, UUID viewerId, MenuTaskKey taskKey, long generation) {

  /**
   * Validates and creates the context.
   *
   * @throws NullPointerException if a required component is {@code null}
   * @throws IllegalArgumentException if {@code generation} is not positive
   */
  public PageLoadContext {
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(taskKey, "taskKey");

    if (generation <= 0L) {
      throw new IllegalArgumentException("generation must be greater than zero: " + generation);
    }
  }
}
