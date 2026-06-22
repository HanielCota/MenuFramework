package com.hanielfialho.menuframework.api;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.event.inventory.InventoryType;
import org.junit.jupiter.api.Test;

final class MenuLayoutTest {

  @Test
  void createsChestLayoutFromRowCount() {
    MenuLayout layout = MenuLayout.chest(6);

    assertEquals(InventoryType.CHEST, layout.inventoryType());
    assertEquals(6, layout.rows());
    assertEquals(54, layout.size());
  }

  @Test
  void convertsCoordinatesAndSlotsUsingZeroBasedIndexes() {
    MenuLayout layout = MenuLayout.chest(4);

    assertEquals(0, layout.slot(0, 0));
    assertEquals(17, layout.slot(1, 8));
    assertEquals(3, layout.rowOf(35));
    assertEquals(8, layout.columnOf(35));
  }

  @Test
  void validatesChestRowCount() {
    assertThrows(IllegalArgumentException.class, () -> MenuLayout.chest(0));

    assertThrows(IllegalArgumentException.class, () -> MenuLayout.chest(7));
  }

  @Test
  void validatesCoordinatesAndRawSlots() {
    MenuLayout layout = MenuLayout.chest(2);

    assertThrows(IndexOutOfBoundsException.class, () -> layout.slot(-1, 0));

    assertThrows(IndexOutOfBoundsException.class, () -> layout.slot(0, 9));

    assertThrows(IndexOutOfBoundsException.class, () -> layout.checkSlot(18));
  }

  @Test
  void reportsWhetherRawSlotBelongsToLayout() {
    MenuLayout layout = MenuLayout.chest(1);

    assertTrue(layout.contains(0));
    assertTrue(layout.contains(8));
    assertFalse(layout.contains(-1));
    assertFalse(layout.contains(9));
    assertEquals(8, layout.checkSlot(8));
  }

  @Test
  void layoutsWithSameStructureAreEqual() {
    MenuLayout first = MenuLayout.chest(3);
    MenuLayout second = MenuLayout.chest(3);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertNotEquals(first, MenuLayout.chest(2));
  }
}
