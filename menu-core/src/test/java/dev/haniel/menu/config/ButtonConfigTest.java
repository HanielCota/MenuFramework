package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.item.Icon;
import java.util.List;
import org.junit.jupiter.api.Test;

class ButtonConfigTest {

  @Test
  void defaultsBlankMaterialToStone() {
    assertEquals("STONE", new ButtonConfig(0, "  ", null, null).material());
  }

  @Test
  void defaultsNullNameAndLore() {
    ButtonConfig button = new ButtonConfig(0, "DIRT", null, null);

    assertEquals("", button.name());
    assertTrue(button.lore().isEmpty());
  }

  @Test
  void rejectsNegativeSlot() {
    assertThrows(
        IllegalArgumentException.class, () -> new ButtonConfig(-1, "DIRT", "n", List.of()));
  }

  @Test
  void iconDropsSlotAndKeepsAppearance() {
    ButtonConfig button = new ButtonConfig(5, "DIAMOND", "<gold>Gem", List.of("line-a", "line-b"));

    Icon icon = button.icon();

    assertEquals("DIAMOND", icon.material());
    assertEquals("<gold>Gem", icon.name());
    assertEquals(List.of("line-a", "line-b"), icon.lore());
  }
}
