package com.github.hanielcota.menuframework.core.config;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Centralized configuration validation utilities.
 *
 * <p>Provides fail-fast validation for cache sizes, timeouts, and other numeric configuration
 * values used throughout the framework.
 */
public final class ConfigValidator {

  private ConfigValidator() {}

  /**
   * Validates that the given value is strictly positive.
   *
   * @param value the value to validate
   * @param name the configuration property name for error messages
   * @return the validated value
   * @throws IllegalArgumentException if value is <= 0
   */
  public static int requirePositive(int value, @NonNull String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " must be positive, got: " + value);
    }
    return value;
  }

  /**
   * Validates that the given value is non-negative.
   *
   * @param value the value to validate
   * @param name the configuration property name for error messages
   * @return the validated value
   * @throws IllegalArgumentException if value < 0
   */
  public static int requireNonNegative(int value, @NonNull String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " cannot be negative, got: " + value);
    }
    return value;
  }

  /**
   * Validates that the given value is non-negative.
   *
   * @param value the value to validate
   * @param name the configuration property name for error messages
   * @return the validated value
   * @throws IllegalArgumentException if value < 0
   */
  public static long requireNonNegative(long value, @NonNull String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " cannot be negative, got: " + value);
    }
    return value;
  }

  /**
   * Validates that the given value is within the specified inclusive range.
   *
   * @param value the value to validate
   * @param min the minimum allowed value
   * @param max the maximum allowed value
   * @param name the configuration property name for error messages
   * @return the validated value
   * @throws IllegalArgumentException if value is outside [min, max]
   */
  public static int requireInRange(int value, int min, int max, @NonNull String name) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          String.format("%s must be between %d and %d, got: %d", name, min, max, value));
    }
    return value;
  }

  /**
   * Validates that the given percentage is within [0.0, 1.0].
   *
   * @param value the percentage to validate
   * @param name the configuration property name for error messages
   * @return the validated value
   * @throws IllegalArgumentException if value is outside [0.0, 1.0]
   */
  public static double requirePercentage(double value, @NonNull String name) {
    if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0 || value > 1.0) {
      throw new IllegalArgumentException(name + " must be between 0.0 and 1.0, got: " + value);
    }
    return value;
  }

  /**
   * Validates that the given string is non-null and non-blank.
   *
   * @param value the string to validate
   * @param name the configuration property name for error messages
   * @return the validated string
   * @throws IllegalArgumentException if value is null or blank
   */
  public static @NonNull String requireNonBlank(@NonNull String value, @NonNull String name) {
    Objects.requireNonNull(value, name + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " cannot be blank");
    }
    return value;
  }
}
