package com.github.hanielcota.menuframework.pagination;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PageView Tests")
class PageViewTest {

  @Test
  @DisplayName("Should create valid page view")
  void shouldCreateValidPageView() {
    var items = new ItemStack[27];
    items[0] = new ItemStack(Material.STONE);
    var page = new PageView(0, items, 5);

    assertEquals(0, page.pageNumber());
    assertEquals(5, page.totalPages());
    assertNotNull(page.items());
    assertEquals(27, page.items().length);
  }

  @Test
  @DisplayName("Should reject negative page number")
  void shouldRejectNegativePageNumber() {
    assertThrows(IllegalArgumentException.class, () ->
        new PageView(-1, new ItemStack[0], 1));
  }

  @Test
  @DisplayName("Should reject zero total pages")
  void shouldRejectZeroTotalPages() {
    assertThrows(IllegalArgumentException.class, () ->
        new PageView(0, new ItemStack[0], 0));
  }

  @Test
  @DisplayName("Should handle null items array")
  void shouldHandleNullItems() {
    var page = new PageView(0, null, 1);
    assertNotNull(page.items());
    assertEquals(0, page.items().length);
  }

  @Test
  @DisplayName("Should clone items defensively")
  void shouldCloneItemsDefensively() {
    var items = new ItemStack[1];
    items[0] = new ItemStack(Material.STONE);
    var page = new PageView(0, items, 1);

    // Modify original array
    items[0] = new ItemStack(Material.DIRT);

    // PageView should still have stone
    assertEquals(Material.STONE, page.get(0).getType());
  }

  @Test
  @DisplayName("Should get item by slot")
  void shouldGetItemBySlot() {
    var items = new ItemStack[5];
    items[2] = new ItemStack(Material.STONE);
    var page = new PageView(0, items, 1);

    assertNotNull(page.get(2));
    assertEquals(Material.STONE, page.get(2).getType());
    assertNull(page.get(0));
    assertNull(page.get(10)); // Out of bounds
  }

  @Test
  @DisplayName("Should clone item on get")
  void shouldCloneItemOnGet() {
    var items = new ItemStack[1];
    items[0] = new ItemStack(Material.STONE);
    var page = new PageView(0, items, 1);

    var item1 = page.get(0);
    var item2 = page.get(0);
    assertNotSame(item1, item2);
  }

  @Test
  @DisplayName("Should be equal with same content")
  void shouldBeEqualWithSameContent() {
    var page1 = new PageView(0, new ItemStack[0], 1);
    var page2 = new PageView(0, new ItemStack[0], 1);

    assertEquals(page1, page2);
    assertEquals(page1.hashCode(), page2.hashCode());
  }

  @Test
  @DisplayName("Should not be equal with different page number")
  void shouldNotBeEqualWithDifferentPage() {
    var page1 = new PageView(0, new ItemStack[0], 1);
    var page2 = new PageView(1, new ItemStack[0], 1);

    assertNotEquals(page1, page2);
  }

  @Test
  @DisplayName("Should generate valid toString")
  void shouldGenerateValidToString() {
    var page = new PageView(0, new ItemStack[0], 1);
    var str = page.toString();

    assertTrue(str.contains("PageView"));
    assertTrue(str.contains("pageNumber=0"));
    assertTrue(str.contains("totalPages=1"));
  }
}
