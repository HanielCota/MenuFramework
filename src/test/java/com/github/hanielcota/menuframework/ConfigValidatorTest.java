package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import com.github.hanielcota.menuframework.internal.config.ConfigValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for ConfigValidator.
 */
@DisplayName("ConfigValidator Tests")
class ConfigValidatorTest {

  @Test
  @DisplayName("Should accept positive value")
  void shouldAcceptPositive() {
    assertEquals(10, ConfigValidator.requirePositive(10, "test"));
  }

  @Test
  @DisplayName("Should reject zero as positive")
  void shouldRejectZeroAsPositive() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePositive(0, "test"));
  }

  @Test
  @DisplayName("Should reject negative as positive")
  void shouldRejectNegativeAsPositive() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePositive(-5, "test"));
  }

  @Test
  @DisplayName("Should accept non-negative values")
  void shouldAcceptNonNegative() {
    assertEquals(0, ConfigValidator.requireNonNegative(0, "test"));
    assertEquals(100, ConfigValidator.requireNonNegative(100, "test"));
  }

  @Test
  @DisplayName("Should reject negative for non-negative")
  void shouldRejectNegativeForNonNegative() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireNonNegative(-1, "test"));
  }

  @Test
  @DisplayName("Should accept value in range")
  void shouldAcceptInRange() {
    assertEquals(5, ConfigValidator.requireInRange(5, 1, 10, "test"));
  }

  @Test
  @DisplayName("Should reject value below range")
  void shouldRejectBelowRange() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireInRange(0, 1, 10, "test"));
  }

  @Test
  @DisplayName("Should reject value above range")
  void shouldRejectAboveRange() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireInRange(11, 1, 10, "test"));
  }

  @Test
  @DisplayName("Should accept valid percentages")
  void shouldAcceptValidPercentages() {
    assertEquals(0.0, ConfigValidator.requirePercentage(0.0, "test"));
    assertEquals(0.5, ConfigValidator.requirePercentage(0.5, "test"));
    assertEquals(1.0, ConfigValidator.requirePercentage(1.0, "test"));
  }

  @Test
  @DisplayName("Should reject percentage above 1.0")
  void shouldRejectAboveOne() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(1.1, "test"));
  }

  @Test
  @DisplayName("Should reject negative percentage")
  void shouldRejectNegativePercentage() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(-0.1, "test"));
  }

  @Test
  @DisplayName("Should reject NaN percentage")
  void shouldRejectNaN() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(Double.NaN, "test"));
  }

  @Test
  @DisplayName("Should reject infinite percentage")
  void shouldRejectInfinite() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(Double.POSITIVE_INFINITY, "test"));
  }

  @Test
  @DisplayName("Should accept non-blank string")
  void shouldAcceptNonBlank() {
    assertEquals("test", ConfigValidator.requireNonBlank("test", "field"));
  }

  @Test
  @DisplayName("Should reject blank string")
  void shouldRejectBlank() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireNonBlank("   ", "field"));
  }

  @Test
  @DisplayName("Should reject empty string")
  void shouldRejectEmpty() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireNonBlank("", "field"));
  }

  @Test
  @DisplayName("Should reject null string")
  void shouldRejectNull() {
    assertThrows(NullPointerException.class, () ->
        ConfigValidator.requireNonBlank(null, "field"));
  }
}
