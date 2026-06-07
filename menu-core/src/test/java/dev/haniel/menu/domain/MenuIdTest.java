package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MenuIdTest {

  @Test
  void keepsValidValue() {
    assertEquals("hello", new MenuId("hello").value());
  }

  @Test
  void rejectsBlank() {
    assertThrows(IllegalArgumentException.class, () -> new MenuId(" "));
  }

  @Test
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> new MenuId(null));
  }

  @Test
  void rejectsPathTraversal() {
    assertThrows(IllegalArgumentException.class, () -> new MenuId("../secret"));
  }

  @Test
  void rejectsUppercaseAndSeparators() {
    assertThrows(IllegalArgumentException.class, () -> new MenuId("Hello"));
    assertThrows(IllegalArgumentException.class, () -> new MenuId("menus/hello"));
  }

  @Test
  void keepsDigitsHyphensAndUnderscores() {
    assertEquals("no-nav_2", new MenuId("no-nav_2").value());
  }

  @Test
  void rejectsAnExcessivelyLongId() {
    assertThrows(IllegalArgumentException.class, () -> new MenuId("a".repeat(65)));
  }

  @Test
  void keepsAnIdAtTheLengthLimit() {
    String maxLength = "a".repeat(64);
    assertEquals(maxLength, new MenuId(maxLength).value());
  }
}
