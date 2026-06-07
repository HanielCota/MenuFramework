package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ButtonIdTest {

  @Test
  void keepsTheGivenValue() {
    assertEquals("confirm", new ButtonId("confirm").value());
  }

  @Test
  void rejectsNullValue() {
    assertThrows(IllegalArgumentException.class, () -> new ButtonId(null));
  }

  @Test
  void rejectsBlankValue() {
    assertThrows(IllegalArgumentException.class, () -> new ButtonId("   "));
  }
}
