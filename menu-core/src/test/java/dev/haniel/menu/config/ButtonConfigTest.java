package dev.haniel.menu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.ItemFlag;
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

  @Test
  void plainConstructorUsesDefaultTraits() {
    Icon icon = new ButtonConfig(0, "STONE", "", List.of()).icon();

    assertEquals(1, icon.traits().amount());
    assertTrue(icon.traits().flags().isEmpty());
  }

  @Test
  void iconCarriesRichTraits() {
    ButtonConfig button =
        new ButtonConfig(
            0, "DIAMOND", "g", List.of(), 8, true, true, 5, List.of(ItemFlag.HIDE_ATTRIBUTES));

    Icon icon = button.icon();

    assertEquals(8, icon.traits().amount());
    assertTrue(icon.traits().glowing());
    assertTrue(icon.traits().unbreakable());
    assertEquals(5, icon.traits().customModelData().orElseThrow());
    assertTrue(icon.traits().flags().contains(ItemFlag.HIDE_ATTRIBUTES));
  }

  @Test
  void clampsInvalidAmountToOne() {
    assertEquals(
        1, new ButtonConfig(0, "STONE", "", List.of(), 0, false, false, 0, List.of()).amount());
  }

  @Test
  void nonPositiveModelDataMeansNone() {
    Icon icon = new ButtonConfig(0, "STONE", "", List.of(), 1, false, false, 0, List.of()).icon();

    assertTrue(icon.traits().customModelData().isEmpty());
  }
}
