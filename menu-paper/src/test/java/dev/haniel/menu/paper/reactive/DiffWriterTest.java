package dev.haniel.menu.paper.reactive;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class DiffWriterTest {

  @Test
  void exposesBackingInventory() {
    Inventory inventory = inventoryOfSize(9);

    DiffWriter writer = new DiffWriter(inventory);

    assertSame(inventory, writer.inventory());
  }

  @Test
  void firstWriteSetsEveryNonNullSlotAndSkipsNulls() {
    Inventory inventory = inventoryOfSize(3);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack first = mock(ItemStack.class);
    ItemStack second = mock(ItemStack.class);

    writer.write(new ItemStack[] {first, null, second});

    verify(inventory).setItem(0, first);
    verify(inventory, never()).setItem(1, null);
    verify(inventory).setItem(2, second);
  }

  @Test
  void unchangedSlotsAreNotRewrittenOnTheSecondWrite() {
    Inventory inventory = inventoryOfSize(2);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack stable = mock(ItemStack.class);
    ItemStack original = mock(ItemStack.class);
    writer.write(new ItemStack[] {stable, original});

    ItemStack replacement = mock(ItemStack.class);
    writer.write(new ItemStack[] {stable, replacement});

    verify(inventory).setItem(0, stable);
    verify(inventory).setItem(1, replacement);
  }

  @Test
  void anUnchangedItemIsNeverReset() {
    Inventory inventory = inventoryOfSize(1);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack item = mock(ItemStack.class);

    writer.write(new ItemStack[] {item});
    writer.write(new ItemStack[] {item});

    verify(inventory, times(1)).setItem(0, item);
  }

  @Test
  void clearingASlotWritesNull() {
    Inventory inventory = inventoryOfSize(1);
    DiffWriter writer = new DiffWriter(inventory);
    ItemStack item = mock(ItemStack.class);
    writer.write(new ItemStack[] {item});

    writer.write(new ItemStack[] {null});

    verify(inventory).setItem(0, null);
  }

  private static Inventory inventoryOfSize(int size) {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(size);
    return inventory;
  }
}
