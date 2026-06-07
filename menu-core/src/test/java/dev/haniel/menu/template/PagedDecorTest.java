package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PagedDecorTest {

  @Test
  void exposesTheThreeVisuals() {
    PagedDecor<String> decor = new PagedDecor<>("prev", "next", "border");

    assertEquals("prev", decor.previous());
    assertEquals("next", decor.next());
    assertEquals("border", decor.border());
  }

  @Test
  void allowsNullControlsWhenTheMaskHasNoNavigation() {
    PagedDecor<String> decor = new PagedDecor<>(null, null, "border");

    assertNull(decor.previous());
    assertNull(decor.next());
    assertEquals("border", decor.border());
  }

  @Test
  void equalsByComponents() {
    PagedDecor<String> first = new PagedDecor<>("prev", "next", "border");
    PagedDecor<String> second = new PagedDecor<>("prev", "next", "border");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }
}
