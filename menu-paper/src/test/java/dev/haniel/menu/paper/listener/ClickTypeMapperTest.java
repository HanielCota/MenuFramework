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
        ClickType.SHIFT_LEFT,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.SHIFT_LEFT));
    assertEquals(
        ClickType.SHIFT_RIGHT,
        ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.SHIFT_RIGHT));
    assertEquals(
        ClickType.MIDDLE, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.MIDDLE));
  }

  @Test
  void mapsUnknownClickTypesToOther() {
    assertEquals(
        ClickType.OTHER, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.DOUBLE_CLICK));
    assertEquals(
        ClickType.OTHER, ClickTypeMapper.map(org.bukkit.event.inventory.ClickType.NUMBER_KEY));
  }
}
