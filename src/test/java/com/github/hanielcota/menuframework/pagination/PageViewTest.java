package com.github.hanielcota.menuframework.pagination;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for PageView defensive validations.
 */
@DisplayName("PageView Tests")
class PageViewTest {

  @Test
  @DisplayName("Should create valid PageView")
  void shouldCreateValidPageView() {
    var items = new ItemStack[9];
    var pageView = new PageView(0, items, 5);

    assertEquals(0, pageView.pageNumber());
    assertEquals(5, pageView.totalPages());
    assertEquals(9, pageView.items().length);
  }

  @Test
  @DisplayName("Should reject negative page number")
  void shouldRejectNegativePageNumber() {
    assertThrows(IllegalArgumentException.class, () ->
        new PageView(-1, new ItemStack[9], 5));
  }

  @Test
  @DisplayName("Should reject zero total pages")
  void shouldRejectZeroTotalPages() {
    assertThrows(IllegalArgumentException.class, () ->
        new PageView(0, new ItemStack[9], 0));
  }

  @Test
  @DisplayName("Should handle null items array")
  void shouldHandleNullItems() {
    var pageView = new PageView(0, null, 1);
    assertEquals(0, pageView.items().length);
  }

  @Test
  @DisplayName("Should clone items defensively")
  void shouldCloneItemsDefensively() {
    var items = new ItemStack[1];
    items[0] = new ItemStack(org.bukkit.Material.DIAMOND);

    var pageView = new PageView(0, items, 1);
    var retrieved = pageView.items();

    assertNotSame(items, retrieved);
    assertEquals(items[0].getType(), retrieved[0].getType());
  }

  @Test
  @DisplayName("Should get item at valid slot")
  void shouldGetItemAtValidSlot() {
    var items = new ItemStack[9];
    items[0] = new ItemStack(org.bukkit.Material.DIAMOND);

    var pageView = new PageView(0, items, 1);
    var item = pageView.get(0);

    assertNotNull(item);
    assertEquals(org.bukkit.Material.DIAMOND, item.getType());
  }

  @Test
  @DisplayName("Should return null for out of bounds slot")
  void shouldReturnNullForOutOfBounds() {
    var pageView = new PageView(0, new ItemStack[9], 1);
    assertNull(pageView.get(-1));
    assertNull(pageView.get(9));
  }

  @Test
  @DisplayName("Should implement equals and hashCode correctly")
  void shouldImplementEqualsAndHashCode() {
    var items1 = new ItemStack[9];
    var items2 = new ItemStack[9];

    var pageView1 = new PageView(0, items1, 1);
    var pageView2 = new PageView(0, items2, 1);

    assertEquals(pageView1, pageView2);
    assertEquals(pageView1.hashCode(), pageView2.hashCode());
  }
}
