package com.hanielfialho.menuframework.api.task;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable logical key for a session-owned operation.
 *
 * <p>At most one task is active for a given key in each session. Starting a new operation with the
 * same key replaces the previous generation.
 *
 * @param value identifier containing between one and 64 lowercase letters, digits, dots,
 *     underscores or hyphens; the first character must be alphanumeric
 */
public record MenuTaskKey(String value) {

  private static final Pattern VALID_VALUE = Pattern.compile("[a-z0-9][a-z0-9._-]{0,63}");

  /**
   * Validates and creates the key.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if {@code value} has an invalid format
   */
  public MenuTaskKey {
    Objects.requireNonNull(value, "value");

    if (!VALID_VALUE.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Task key must match " + VALID_VALUE.pattern() + ": " + value);
    }
  }

  /**
   * Creates a validated task key.
   *
   * @param value textual identifier
   * @return validated key
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if {@code value} has an invalid format
   */
  public static MenuTaskKey of(String value) {
    return new MenuTaskKey(value);
  }
}
