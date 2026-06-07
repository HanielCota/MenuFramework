package dev.haniel.menu.paper.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.click.ClickType;
import org.junit.jupiter.api.Test;

class ClickTypeMapperTest {

  @Test
  void mapsKnownClickTypes() {
    assertEquals(ClickType.LEFT, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.LEFT));
    assertEquals(ClickType.RIGHT, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.RIGHT));
    assertEquals(
        ClickType.SHIFT_LEFT, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.SHIFT_LEFT));
    assertEquals(
        ClickType.SHIFT_RIGHT,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.SHIFT_RIGHT));
    assertEquals(
        ClickType.MIDDLE, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.MIDDLE));
  }

  @Test
  void mapsKeyboardAndDropInteractions() {
    assertEquals(ClickType.DROP, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.DROP));
    assertEquals(
        ClickType.DROP, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.CONTROL_DROP));
    assertEquals(
        ClickType.DOUBLE_CLICK,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.DOUBLE_CLICK));
    assertEquals(
        ClickType.NUMBER_KEY, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.NUMBER_KEY));
    assertEquals(
        ClickType.SWAP_OFFHAND,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.SWAP_OFFHAND));
  }

  @Test
  void mapsUnknownClickTypesToOther() {
    assertEquals(
        ClickType.OTHER, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.CREATIVE));
    assertEquals(
        ClickType.OTHER,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.WINDOW_BORDER_LEFT));
  }
}
