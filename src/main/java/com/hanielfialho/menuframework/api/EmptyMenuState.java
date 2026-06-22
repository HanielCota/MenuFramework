package com.hanielfialho.menuframework.api;

/**
 * Singleton state for menus that do not need per-session data.
 *
 * <p>Using this value keeps the framework's non-null state invariant instead of representing the
 * absence of state with {@code null}.
 */
public enum EmptyMenuState {

  /** The single empty-state instance. */
  INSTANCE
}
