package com.github.hanielcota.menuframework.pagination;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for PageCacheKey defensive validations.
 */
@DisplayName("PageCacheKey Tests")
class PageCacheKeyTest {

  @Test
  @DisplayName("Should create valid key")
  void shouldCreateValidKey() {
    var key = new PageCacheKey("menu1", 0, 12345);
    assertEquals("menu1", key.menuId());
    assertEquals(0, key.pageNumber());
    assertEquals(12345, key.contentHash());
  }

  @Test
  @DisplayName("Should reject null menuId")
  void shouldRejectNullMenuId() {
    assertThrows(NullPointerException.class, () ->
        new PageCacheKey(null, 0, 0));
  }

  @Test
  @DisplayName("Should reject negative page number")
  void shouldRejectNegativePageNumber() {
    assertThrows(IllegalArgumentException.class, () ->
        new PageCacheKey("menu", -1, 0));
  }

  @Test
  @DisplayName("Should implement equals and hashCode correctly")
  void shouldImplementEqualsAndHashCode() {
    var key1 = new PageCacheKey("menu", 0, 123);
    var key2 = new PageCacheKey("menu", 0, 123);

    assertEquals(key1, key2);
    assertEquals(key1.hashCode(), key2.hashCode());
  }

  @Test
  @DisplayName("Should not equal different keys")
  void shouldNotEqualDifferentKeys() {
    var key1 = new PageCacheKey("menu1", 0, 123);
    var key2 = new PageCacheKey("menu2", 0, 123);

    assertNotEquals(key1, key2);
  }
}
