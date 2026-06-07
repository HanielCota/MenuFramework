package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaginationConfigTest {

  @Test
  void emptyWhenMaskIsEmpty() {
    assertTrue(new PaginationConfig(List.of(), null, null).isEmpty());
  }

  @Test
  void emptyWhenMaskIsNull() {
    assertTrue(new PaginationConfig(null, null, null).isEmpty());
  }

  @Test
  void notEmptyWhenMaskHasRows() {
    assertFalse(new PaginationConfig(List.of("XXXXXXXXX"), null, null).isEmpty());
  }

  @Test
  void emptyWhenNavigationButtonsAreSetButMaskIsMissing() {
    ButtonConfig previous = new ButtonConfig(0, "ARROW", "<", List.of());
    ButtonConfig next = new ButtonConfig(8, "ARROW", ">", List.of());

    assertTrue(new PaginationConfig(List.of(), previous, next).isEmpty());
  }

  @Test
  void copiesMaskDefensively() {
    List<String> mask = new ArrayList<>(List.of("XXXXXXXXX"));
    PaginationConfig config = new PaginationConfig(mask, null, null);

    mask.clear();

    assertFalse(config.isEmpty());
  }
}
