package com.hanielfialho.menuframework.api;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Immutable session snapshot supplied to lifecycle callbacks.
 *
 * @param sessionId unique identifier of this menu opening
 * @param viewer player that owns the session
 * @param state current non-null session state
 * @param revision positive revision of the published frame
 * @param historyDepth number of previous menus that can be restored
 * @param <S> session-state type
 */
public record MenuContext<S>(
    UUID sessionId, Player viewer, S state, long revision, int historyDepth)
    implements MenuNavigationContext {

  /**
   * Validates and creates the context snapshot.
   *
   * @throws NullPointerException if a required component is {@code null}
   * @throws IllegalArgumentException if {@code revision} is not positive or {@code historyDepth} is
   *     negative
   */
  public MenuContext {
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(state, "state");

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }
  }

  /**
   * Creates a context with no navigation history.
   *
   * @param sessionId unique session identifier
   * @param viewer session owner
   * @param state current state
   * @param revision current positive revision
   */
  public MenuContext(UUID sessionId, Player viewer, S state, long revision) {
    this(sessionId, viewer, state, revision, 0);
  }
}
