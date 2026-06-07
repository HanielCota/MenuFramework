package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SlotTest {

  @Test
  void acceptsFirstSlot() {
    assertEquals(0, Slot.of(0, 1).value());
  }

  @Test
  void acceptsLastSlotForSixRows() {
    assertEquals(53, Slot.of(53, 6).value());
  }

  @Test
  void rejectsNegativeIndex() {
    assertThrows(IllegalArgumentException.class, () -> Slot.of(-1, 6));
  }

  @Test
  void rejectsIndexBeyondLastSlot() {
    assertThrows(IllegalArgumentException.class, () -> Slot.of(9, 1));
  }
}
