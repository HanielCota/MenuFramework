package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PageNumberTest {

  @Test
  void firstIsZero() {
    assertEquals(0, PageNumber.first().value());
  }

  @Test
  void nextIncrements() {
    assertEquals(1, PageNumber.first().next().value());
  }

  @Test
  void previousClampsAtFirstPage() {
    assertEquals(0, PageNumber.first().previous().value());
  }

  @Test
  void previousDecrementsWhenPastFirst() {
    assertEquals(1, new PageNumber(2).previous().value());
  }

  @Test
  void rejectsNegativeIndex() {
    assertThrows(IllegalArgumentException.class, () -> new PageNumber(-1));
  }
}
