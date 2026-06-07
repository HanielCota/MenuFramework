package dev.haniel.menu.paper.render.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dev.haniel.menu.domain.MenuId;
import org.junit.jupiter.api.Test;

class PageKeyTest {

  @Test
  void equalKeysShareHashCodeAndEquality() {
    PageKey first = new PageKey(new MenuId("shop"), 2, 7L, 31);
    PageKey second = new PageKey(new MenuId("shop"), 2, 7L, 31);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void differentPageMakesDistinctKeys() {
    PageKey first = new PageKey(new MenuId("shop"), 0, 0L, 0);
    PageKey second = new PageKey(new MenuId("shop"), 1, 0L, 0);

    assertNotEquals(first, second);
  }

  @Test
  void differentVersionMakesDistinctKeys() {
    PageKey first = new PageKey(new MenuId("shop"), 0, 0L, 0);
    PageKey second = new PageKey(new MenuId("shop"), 0, 1L, 0);

    assertNotEquals(first, second);
  }

  @Test
  void differentContentHashMakesDistinctKeys() {
    PageKey first = new PageKey(new MenuId("shop"), 0, 0L, 0);
    PageKey second = new PageKey(new MenuId("shop"), 0, 0L, 99);

    assertNotEquals(first, second);
  }

  @Test
  void differentMenuMakesDistinctKeys() {
    PageKey first = new PageKey(new MenuId("shop"), 0, 0L, 0);
    PageKey second = new PageKey(new MenuId("bank"), 0, 0L, 0);

    assertNotEquals(first, second);
  }

  @Test
  void exposesItsComponents() {
    PageKey key = new PageKey(new MenuId("shop"), 4, 9L, 123);

    assertEquals(new MenuId("shop"), key.menuId());
    assertEquals(4, key.page());
    assertEquals(9L, key.version());
    assertEquals(123, key.contentHash());
  }
}
