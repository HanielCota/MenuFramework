package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Adversarial boundary probes for {@link Slot}. */
class SlotEdgeCasesTest {

  @Test
  void acceptsLastSlotOfSingleRow() {
    assertEquals(8, Slot.of(8, 1).value());
  }

  @Test
  void rejectsSlotOneBeyondSingleRow() {
    assertThrows(IllegalArgumentException.class, () -> Slot.of(9, 1));
  }

  @Test
  void acceptsLastSlotOfThreeRows() {
    assertEquals(26, Slot.of(26, 3).value());
  }

  @Test
  void rejectsFirstSlotOutOfThreeRowBounds() {
    assertThrows(IllegalArgumentException.class, () -> Slot.of(27, 3));
  }

  @Test
  void rejectsAnySlotWhenRowsIsZero() {
    // rows == 0 gives max == -1; even slot 0 must be rejected, never accepted.
    assertThrows(IllegalArgumentException.class, () -> Slot.of(0, 0));
  }
}
