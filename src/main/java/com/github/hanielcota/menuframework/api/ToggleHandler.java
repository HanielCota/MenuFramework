package com.github.hanielcota.menuframework.api;

import org.jspecify.annotations.NonNull;

/**
 * Functional interface for toggle slot handlers.
 *
 * <p>Called when a player clicks a toggle slot, passing the new state.
 */
@FunctionalInterface
public interface ToggleHandler {

  /**
   * Called when the toggle state changes.
   *
   * @param context the click context
   * @param enabled the new state (true = enabled, false = disabled)
   */
  void onToggle(@NonNull ClickContext context, boolean enabled);
}
