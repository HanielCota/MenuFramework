package com.hanielfialho.menuframework.api;

import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * Immutable snapshot supplied during a menu render.
 *
 * @param viewer player viewing the menu
 * @param state candidate state for the frame being rendered
 * @param historyDepth number of previous menus that can be restored
 * @param <S> session-state type
 */
public record MenuRenderContext<S>(Player viewer, S state, int historyDepth)
    implements MenuNavigationContext {

  /**
   * Validates and creates the render snapshot.
   *
   * @throws NullPointerException if {@code viewer} or {@code state} is {@code null}
   * @throws IllegalArgumentException if {@code historyDepth} is negative
   */
  public MenuRenderContext {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(state, "state");

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }
  }

  /**
   * Creates a render snapshot with no navigation history.
   *
   * @param viewer player viewing the menu
   * @param state candidate state
   */
  public MenuRenderContext(Player viewer, S state) {
    this(viewer, state, 0);
  }
}
