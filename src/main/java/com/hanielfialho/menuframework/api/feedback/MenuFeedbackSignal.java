package com.hanielfialho.menuframework.api.feedback;

import java.util.Objects;

/** Stable identifier of a visual, auditory or telemetry feedback signal. */
public record MenuFeedbackSignal(String value) {

  /**
   * Validates the identifier.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if it is blank or padded with whitespace
   */
  public MenuFeedbackSignal {
    Objects.requireNonNull(value, "value");

    if (value.isBlank()) {
      throw new IllegalArgumentException("Feedback signal cannot be blank");
    }

    if (!value.equals(value.strip())) {
      throw new IllegalArgumentException(
          "Feedback signal cannot contain leading or trailing whitespace: '" + value + "'");
    }
  }

  /**
   * Creates a signal.
   *
   * @param value stable identifier
   * @return validated signal
   */
  public static MenuFeedbackSignal of(String value) {
    return new MenuFeedbackSignal(value);
  }
}
