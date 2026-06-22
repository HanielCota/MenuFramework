package com.hanielfialho.menuframework.api;

import static org.junit.jupiter.api.Assertions.*;

import com.hanielfialho.menuframework.api.layout.SlotPatterns;
import java.util.List;
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

  @Test
  void namedSlotsAndRegionsAreResolvedFromBuilderMetadata() {
    MenuLayout layout =
        MenuLayout.chestBuilder(3)
            .slot("close", 2, 8)
            .region("content", SlotPatterns.rectangle(1, 1, 2, 3))
            .build();

    assertEquals(26, layout.slot("close"));
    assertTrue(layout.findSlot("close").isPresent());
    assertTrue(layout.hasSlot("close"));
    assertEquals(List.of(10, 11, 12, 19, 20, 21), layout.region("content").slots());
    assertTrue(layout.findRegion("content").isPresent());
    assertTrue(layout.hasRegion("content"));
    assertEquals(List.of("close"), List.copyOf(layout.namedSlots().keySet()));
    assertEquals(List.of("content"), List.copyOf(layout.namedRegions().keySet()));
  }

  @Test
  void slotPatternsPreserveDeterministicOrder() {
    MenuLayout layout = MenuLayout.chest(3);

    assertEquals(
        List.of(9, 10, 11, 12, 13, 14, 15, 16, 17), SlotPatterns.row(1).resolve(layout).slots());
    assertEquals(List.of(2, 11, 20), SlotPatterns.column(2).resolve(layout).slots());
    assertEquals(
        List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
        SlotPatterns.border().resolve(layout).slots());
    assertEquals(
        List.of(0, 2, 4, 6, 8),
        SlotPatterns.checkerboard(true).resolve(MenuLayout.chest(1)).slots());
  }

  @Test
  void namedMetadataRejectsDuplicateNamesAndSlotOwners() {
    assertThrows(
        IllegalArgumentException.class,
        () -> MenuLayout.chestBuilder(1).slot("left", 0).slot("right", 0));

    assertThrows(
        IllegalArgumentException.class,
        () -> MenuLayout.chestBuilder(1).slot("action", 0).region("action", SlotPatterns.slots(1)));
  }
}
