package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MenuConfigTest {

  @Test
  void paginationAbsentWhenNull() {
    MenuConfig config = new MenuConfig("title", 6, Map.of(), null);

    assertTrue(config.paginationConfig().isEmpty());
  }

  @Test
  void paginationAbsentWhenMaskMissing() {
    PaginationConfig pagination = new PaginationConfig(List.of(), null, null);
    MenuConfig config = new MenuConfig("title", 6, Map.of(), pagination);

    assertTrue(config.paginationConfig().isEmpty());
  }

  @Test
  void paginationPresentWhenMaskSet() {
    PaginationConfig pagination = new PaginationConfig(List.of("XXXXXXXXX"), null, null);
    MenuConfig config = new MenuConfig("title", 1, Map.of(), pagination);

    assertTrue(config.paginationConfig().isPresent());
  }

  @Test
  void rejectsRowsOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> new MenuConfig("t", 0, Map.of(), null));
    assertThrows(IllegalArgumentException.class, () -> new MenuConfig("t", 7, Map.of(), null));
  }

  @Test
  void defaultsNullTitleAndButtons() {
    MenuConfig config = new MenuConfig(null, 3, null, null);

    assertEquals("", config.title());
    assertTrue(config.buttons().isEmpty());
    assertEquals(27, config.size());
  }
}
