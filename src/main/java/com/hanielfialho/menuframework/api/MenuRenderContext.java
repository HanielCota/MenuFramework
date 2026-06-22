package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.api.theme.MenuTheme;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * Immutable snapshot supplied during a menu render.
 *
 * @param viewer player viewing the menu
 * @param state candidate state for the frame being rendered
 * @param historyDepth number of previous menus that can be restored
 * @param theme resolved session theme
 * @param <S> session-state type
 */
public record MenuRenderContext<S>(Player viewer, S state, int historyDepth, MenuTheme theme)
    implements MenuNavigationContext {

  /**
   * Validates and creates the render snapshot.
   *
   * @throws NullPointerException if an object component is {@code null}
   * @throws IllegalArgumentException if {@code historyDepth} is negative
   */
  public MenuRenderContext {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(theme, "theme");

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }
  }

  /**
   * Creates a render snapshot with an explicit history depth and the default theme.
   *
   * @param viewer player viewing the menu
   * @param state candidate state
   * @param historyDepth non-negative history depth
   */
  public MenuRenderContext(Player viewer, S state, int historyDepth) {
    this(viewer, state, historyDepth, MenuTheme.defaults());
  }

  /**
   * Creates a render snapshot with no navigation history and the default theme.
   *
   * @param viewer player viewing the menu
   * @param state candidate state
   */
  public MenuRenderContext(Player viewer, S state) {
    this(viewer, state, 0, MenuTheme.defaults());
  }
}
