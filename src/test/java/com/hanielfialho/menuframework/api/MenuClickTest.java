package com.hanielfialho.menuframework.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.junit.jupiter.api.Test;

final class MenuClickTest {

  @Test
  void detectsHotbarButton() {
    MenuClick normalClick =
        new MenuClick(0, ClickType.LEFT, InventoryAction.PICKUP_ALL, MenuClick.NO_HOTBAR_BUTTON);

    MenuClick hotbarClick = new MenuClick(0, ClickType.NUMBER_KEY, InventoryAction.HOTBAR_SWAP, 4);

    assertFalse(normalClick.usesHotbarButton());
    assertTrue(hotbarClick.usesHotbarButton());
  }

  @Test
  void validatesRawSlotAndHotbarButton() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new MenuClick(-1, ClickType.LEFT, InventoryAction.NOTHING, -1));

    assertThrows(
        IllegalArgumentException.class,
        () -> new MenuClick(0, ClickType.NUMBER_KEY, InventoryAction.HOTBAR_SWAP, 9));
  }
}
