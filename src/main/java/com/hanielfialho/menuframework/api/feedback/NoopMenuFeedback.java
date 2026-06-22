package com.hanielfialho.menuframework.api.feedback;

/** Shared no-op feedback implementation. */
public enum NoopMenuFeedback implements MenuFeedback {
  INSTANCE;

  /** {@inheritDoc} */
  @Override
  public void emit(MenuFeedbackContext context, MenuFeedbackSignal signal) {
    // Intentionally empty.
  }
}
