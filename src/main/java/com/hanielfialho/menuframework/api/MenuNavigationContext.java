package com.hanielfialho.menuframework.api;

/** Read-only navigation-history information for the current session. */
public interface MenuNavigationContext {

  /**
   * Returns the number of previous menus that can be restored.
   *
   * @return a non-negative history depth
   */
  int historyDepth();

  /**
   * Returns whether at least one previous menu can be restored.
   *
   * @return {@code true} when a back operation is available
   */
  default boolean canGoBack() {
    return this.historyDepth() > 0;
  }
}
