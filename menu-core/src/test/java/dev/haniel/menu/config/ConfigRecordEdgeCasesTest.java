package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Edge-case probes for the immutable config records the loader produces: default coalescing,
 * range checks, immutability and the pagination "empty" heuristic.
 */
class ConfigRecordEdgeCasesTest {

  // ---------------------------------------------------------------------------------------------
  // MenuConfig
  // ---------------------------------------------------------------------------------------------

  @Test
  void menuConfigRejectsRowsZero() {
    assertThrows(IllegalArgumentException.class, () -> new MenuConfig("t", 0, Map.of(), null));
  }

  @Test
  void menuConfigRejectsRowsAboveSix() {
    assertThrows(IllegalArgumentException.class, () -> new MenuConfig("t", 7, Map.of(), null));
  }

  @Test
  void menuConfigDefaultsNullTitleToEmpty() {
    assertEquals("", new MenuConfig(null, 1, Map.of(), null).title());
  }

  @Test
  void menuConfigDefaultsNullButtonsToEmpty() {
    assertTrue(new MenuConfig("t", 1, null, null).buttons().isEmpty());
  }

  @Test
  void menuConfigButtonsAreImmutable() {
    MenuConfig config =
        new MenuConfig("t", 1, Map.of("x", new ButtonConfig(0, "STONE", "", List.of())), null);
    assertThrows(UnsupportedOperationException.class, () -> config.buttons().clear());
  }

  @Test
  void menuConfigSizeIsRowsTimesNine() {
    assertEquals(54, new MenuConfig("t", 6, Map.of(), null).size());
  }

  @Test
  void menuConfigTreatsNullPaginationAsStatic() {
    assertTrue(new MenuConfig("t", 1, Map.of(), null).paginationConfig().isEmpty());
  }

  @Test
  void menuConfigTreatsMasklessPaginationAsStatic() {
    PaginationConfig maskless =
        new PaginationConfig(
            List.of(), new ButtonConfig(0, "ARROW", "", List.of()),
            new ButtonConfig(0, "ARROW", "", List.of()));
    assertTrue(new MenuConfig("t", 6, Map.of(), maskless).paginationConfig().isEmpty());
  }

  @Test
  void menuConfigExposesPaginationWhenMaskPresent() {
    PaginationConfig paged =
        new PaginationConfig(
            List.of("XXXXXXXXX"), new ButtonConfig(0, "ARROW", "", List.of()),
            new ButtonConfig(0, "ARROW", "", List.of()));
    assertTrue(new MenuConfig("t", 1, Map.of(), paged).paginationConfig().isPresent());
  }

  // ---------------------------------------------------------------------------------------------
  // ButtonConfig
  // ---------------------------------------------------------------------------------------------

  @Test
  void buttonConfigRejectsNegativeSlot() {
    assertThrows(
        IllegalArgumentException.class, () -> new ButtonConfig(-1, "STONE", "", List.of()));
  }

  @Test
  void buttonConfigDefaultsBlankMaterialToStone() {
    assertEquals("STONE", new ButtonConfig(0, "   ", "", List.of()).material());
  }

  @Test
  void buttonConfigDefaultsNullMaterialToStone() {
    assertEquals("STONE", new ButtonConfig(0, null, "", List.of()).material());
  }

  @Test
  void buttonConfigDefaultsNullNameToEmpty() {
    assertEquals("", new ButtonConfig(0, "STONE", null, List.of()).name());
  }

  @Test
  void buttonConfigDefaultsNullLoreToEmpty() {
    assertTrue(new ButtonConfig(0, "STONE", "", null).lore().isEmpty());
  }

  @Test
  void buttonConfigLoreIsImmutable() {
    ButtonConfig button = new ButtonConfig(0, "STONE", "", List.of("a"));
    assertThrows(UnsupportedOperationException.class, () -> button.lore().add("b"));
  }

  @Test
  void buttonConfigIconCarriesMaterialNameAndLore() {
    ButtonConfig button = new ButtonConfig(0, "EMERALD", "shiny", List.of("line"));
    assertEquals("EMERALD", button.icon().material());
    assertEquals("shiny", button.icon().name());
    assertEquals(List.of("line"), button.icon().lore());
  }

  // ---------------------------------------------------------------------------------------------
  // PaginationConfig
  // ---------------------------------------------------------------------------------------------

  @Test
  void paginationConfigDefaultsNullMaskToEmpty() {
    PaginationConfig pagination =
        new PaginationConfig(null, new ButtonConfig(0, "ARROW", "", List.of()), null);
    assertTrue(pagination.mask().isEmpty());
    assertTrue(pagination.isEmpty());
  }

  @Test
  void paginationConfigMaskIsImmutable() {
    PaginationConfig pagination =
        new PaginationConfig(List.of("XXXXXXXXX"), null, null);
    assertThrows(UnsupportedOperationException.class, () -> pagination.mask().add("more"));
  }

  @Test
  void paginationConfigWithMaskIsNotEmpty() {
    assertFalse(new PaginationConfig(List.of("XXXXXXXXX"), null, null).isEmpty());
  }
}
