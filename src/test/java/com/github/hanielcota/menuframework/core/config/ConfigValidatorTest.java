package com.github.hanielcota.menuframework.core.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigValidator Tests")
class ConfigValidatorTest {

  @Test
  @DisplayName("Should accept positive value")
  void shouldAcceptPositive() {
    assertEquals(5, ConfigValidator.requirePositive(5, "test"));
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
        ConfigValidator.requirePositive(-1, "test"));
  }

  @Test
  @DisplayName("Should accept non-negative value")
  void shouldAcceptNonNegative() {
    assertEquals(0, ConfigValidator.requireNonNegative(0, "test"));
    assertEquals(5, ConfigValidator.requireNonNegative(5, "test"));
  }

  @Test
  @DisplayName("Should reject negative value")
  void shouldRejectNegative() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireNonNegative(-1, "test"));
  }

  @Test
  @DisplayName("Should accept value in range")
  void shouldAcceptInRange() {
    assertEquals(5, ConfigValidator.requireInRange(5, 0, 10, "test"));
    assertEquals(0, ConfigValidator.requireInRange(0, 0, 10, "test"));
    assertEquals(10, ConfigValidator.requireInRange(10, 0, 10, "test"));
  }

  @Test
  @DisplayName("Should reject value below range")
  void shouldRejectBelowRange() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireInRange(-1, 0, 10, "test"));
  }

  @Test
  @DisplayName("Should reject value above range")
  void shouldRejectAboveRange() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requireInRange(11, 0, 10, "test"));
  }

  @Test
  @DisplayName("Should accept valid percentage")
  void shouldAcceptValidPercentage() {
    assertEquals(0.0, ConfigValidator.requirePercentage(0.0, "test"));
    assertEquals(0.5, ConfigValidator.requirePercentage(0.5, "test"));
    assertEquals(1.0, ConfigValidator.requirePercentage(1.0, "test"));
  }

  @Test
  @DisplayName("Should reject negative percentage")
  void shouldRejectNegativePercentage() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(-0.1, "test"));
  }

  @Test
  @DisplayName("Should reject percentage above 1.0")
  void shouldRejectAboveOnePercentage() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(1.1, "test"));
  }

  @Test
  @DisplayName("Should reject NaN percentage")
  void shouldRejectNanPercentage() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(Double.NaN, "test"));
  }

  @Test
  @DisplayName("Should reject infinite percentage")
  void shouldRejectInfinitePercentage() {
    assertThrows(IllegalArgumentException.class, () ->
        ConfigValidator.requirePercentage(Double.POSITIVE_INFINITY, "test"));
  }

  @Test
  @DisplayName("Should accept non-blank string")
  void shouldAcceptNonBlank() {
    assertEquals("test", ConfigValidator.requireNonBlank("test", "field"));
  }

  @Test
  @DisplayName("Should reject null string")
  void shouldRejectNull() {
    assertThrows(NullPointerException.class, () ->
        ConfigValidator.requireNonBlank(null, "field"));
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
}
