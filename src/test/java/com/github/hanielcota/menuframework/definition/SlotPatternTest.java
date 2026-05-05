package com.github.hanielcota.menuframework.definition;

import static org.junit.jupiter.api.Assertions.*;

import com.github.hanielcota.menuframework.api.ClickHandler;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for SlotPattern functionality.
 */
@DisplayName("SlotPattern Tests")
class SlotPatternTest {

  @Test
  @DisplayName("FULL pattern should return all slots")
  void fullPatternShouldReturnAllSlots() {
    var slots = SlotPattern.FULL.slots(3);
    assertEquals(27, slots.size()); // 3 rows * 9 columns
  }

  @Test
  @DisplayName("BORDERED pattern should exclude border")
  void borderedPatternShouldExcludeBorder() {
    var slots = SlotPattern.BORDERED.slots(3);
    assertEquals(7, slots.size()); // 1 row (middle) * 7 columns (excluding first and last)
  }

  @Test
  @DisplayName("CHEST_6 pattern should handle smaller rows")
  void chest6PatternShouldHandleSmallerRows() {
    var slots = SlotPattern.CHEST_6.slots(4);
    assertEquals(36, slots.size()); // 4 rows * 9 columns
  }

  @Test
  @DisplayName("Should default to 6 rows")
  void shouldDefaultTo6Rows() {
    var slots = SlotPattern.FULL.slots();
    assertEquals(54, slots.size());
  }

  @Test
  @DisplayName("Should reject rows below minimum")
  void shouldRejectRowsBelowMin() {
    assertThrows(IllegalArgumentException.class, () ->
        SlotPattern.FULL.slots(0));
  }

  @Test
  @DisplayName("Should reject rows above maximum")
  void shouldRejectRowsAboveMax() {
    assertThrows(IllegalArgumentException.class, () ->
        SlotPattern.FULL.slots(7));
  }
}
