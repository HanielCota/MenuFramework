package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.paper.render.model.RenderedPage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class PageCursorTest {

  @Test
  void startsOnTheFirstPageWithNoBoundActions() {
    PageCursor cursor = new PageCursor(inventoryOfSize(9));

    assertEquals(PageNumber.first(), cursor.page());
    assertFalse(cursor.hasPrevious());
    assertFalse(cursor.hasNext());
    assertTrue(cursor.actionAt(0).isEmpty());
  }

  @Test
  void exposesTheBackingInventory() {
    Inventory inventory = inventoryOfSize(9);

    assertSame(inventory, new PageCursor(inventory).inventory());
  }

  @Test
  void applyRecordsPageAndNavigationFlags() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(new PageNumber(2), new MenuAction[3], true, true));

    assertEquals(new PageNumber(2), cursor.page());
    assertTrue(cursor.hasPrevious());
    assertTrue(cursor.hasNext());
  }

  @Test
  void applyWritesTheRenderedSlotsIntoTheInventory() {
    Inventory inventory = inventoryOfSize(2);
    PageCursor cursor = new PageCursor(inventory);
    ItemStack item = mock(ItemStack.class);

    cursor.apply(
        new RenderedPage(
            PageNumber.first(), new ItemStack[] {item, null}, new MenuAction[2], false, false));

    verify(inventory).setItem(0, item);
  }

  @Test
  void actionAtReturnsTheBoundAction() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));
    MenuAction action = ignored -> {};
    MenuAction[] actions = {null, action, null};

    cursor.apply(page(PageNumber.first(), actions, false, false));

    assertSame(action, cursor.actionAt(1).orElseThrow());
  }

  @Test
  void actionAtIsEmptyForUnboundSlot() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(PageNumber.first(), new MenuAction[3], false, false));

    assertTrue(cursor.actionAt(1).isEmpty());
  }

  @Test
  void actionAtIsEmptyBelowLowerBound() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(PageNumber.first(), new MenuAction[3], false, false));

    assertTrue(cursor.actionAt(-1).isEmpty());
  }

  @Test
  void actionAtIsEmptyAtAndAboveUpperBound() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(PageNumber.first(), new MenuAction[3], false, false));

    assertTrue(cursor.actionAt(3).isEmpty());
    assertTrue(cursor.actionAt(99).isEmpty());
  }

  private static RenderedPage page(
      PageNumber page, MenuAction[] actions, boolean hasPrevious, boolean hasNext) {
    return new RenderedPage(page, new ItemStack[actions.length], actions, hasPrevious, hasNext);
  }

  private static Inventory inventoryOfSize(int size) {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(size);
    return inventory;
  }
}
