package com.hanielfialho.menuframework.testing;

import com.hanielfialho.menuframework.api.Menu;
import java.util.Objects;

/**
 * Navigation request captured by {@link MenuTestHarness}.
 *
 * @param menu target menu
 * @param initialState target initial state
 */
public record MenuTestNavigation(Menu<?> menu, Object initialState) {

  /** Validates the request. */
  public MenuTestNavigation {
    Objects.requireNonNull(menu, "menu");
    Objects.requireNonNull(initialState, "initialState");
  }
}
