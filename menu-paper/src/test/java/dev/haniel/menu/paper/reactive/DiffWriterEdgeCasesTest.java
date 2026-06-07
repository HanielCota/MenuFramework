package dev.haniel.menu.paper.reactive;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial boundary probes for {@link DiffWriter}: the array fed to {@code write} differing in
 * length from the inventory, and the length of the rolling {@code previous} snapshot drifting from
 * the inventory size after a short write.
 */
class DiffWriterEdgeCasesTest {

  @Test
  void neverWritesSlotsBeyondInventorySize() {
    // The rendered array must never be larger than the inventory; if it is, the writer must not
    // blindly call setItem past the inventory's last slot.
    Inventory inventory = inventory(2);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack stone = item("stone");

    ItemStack[] oversized = new ItemStack[] {null, null, stone};

    assertDoesNotThrow(() -> writer.write(oversized));
    verify(inventory, never()).setItem(2, stone);
  }

  @Test
  void handlesShorterThenLongerArrayWithoutIndexError() {
    // previous is replaced wholesale by the last written array. A shorter write shrinks previous;
    // a following full-size write must not blow up reading previous[slot] out of bounds.
    Inventory inventory = inventory(3);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack stone = item("stone");

    writer.write(new ItemStack[] {stone}); // previous now has length 1
    assertDoesNotThrow(() -> writer.write(new ItemStack[] {stone, stone, stone}));
  }

  private static Inventory inventory(int size) {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(size);
    return inventory;
  }

  private static ItemStack item(String name) {
    ItemStack item = mock(ItemStack.class);
    when(item.toString()).thenReturn(name);
    return item;
  }
}
