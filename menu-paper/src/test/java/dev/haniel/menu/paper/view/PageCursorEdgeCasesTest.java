package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.paper.render.model.RenderedPage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial state-after-apply checks for {@link PageCursor}: nav flags and the action map fully
 * replace on each apply (no carry-over), actions route by the applied array's length, and re-apply
 * to an earlier page correctly resets forward/backward flags.
 */
class PageCursorEdgeCasesTest {

  @Test
  void reapplyReplacesActionsWithNoCarryOver() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));
    MenuAction first = ignored -> {};
    MenuAction second = ignored -> {};

    cursor.apply(page(PageNumber.first(), new MenuAction[] {first, null, null}, false, true));
    cursor.apply(page(new PageNumber(1), new MenuAction[] {null, second, null}, true, false));

    assertTrue(cursor.actionAt(0).isEmpty(), "slot 0 must clear after the second apply");
    assertSame(second, cursor.actionAt(1).orElseThrow());
  }

  @Test
  void navFlagsFullyReplaceOnEachApply() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(new PageNumber(2), new MenuAction[3], true, true));
    assertTrue(cursor.hasPrevious());
    assertTrue(cursor.hasNext());

    // Moving to the first page must reset hasPrevious to false and hasNext per the new page.
    cursor.apply(page(PageNumber.first(), new MenuAction[3], false, true));
    assertFalse(cursor.hasPrevious(), "first page must not report a previous page after re-apply");
    assertTrue(cursor.hasNext());

    // Moving to the last page must reset hasNext to false.
    cursor.apply(page(new PageNumber(5), new MenuAction[3], true, false));
    assertTrue(cursor.hasPrevious());
    assertFalse(cursor.hasNext(), "last page must not report a next page after re-apply");
  }

  @Test
  void actionRoutingFollowsTheAppliedArrayLengthNotTheConstructorSize() {
    // Cursor built over a size-9 inventory, but a page with a SHORTER actions array is applied.
    PageCursor cursor = new PageCursor(inventoryOfSize(9));
    MenuAction action = ignored -> {};
    MenuAction[] shortActions = {null, null, action};

    cursor.apply(page(PageNumber.first(), shortActions, false, false));

    assertSame(action, cursor.actionAt(2).orElseThrow());
    // Slots that existed under the old length but not the new one must be out of bounds now.
    assertTrue(cursor.actionAt(3).isEmpty(), "slot beyond the applied array must be empty");
    assertTrue(cursor.actionAt(8).isEmpty());
  }

  @Test
  void pageReflectsTheRenderedPageEvenWhenClampedHigherThanRequested() {
    PageCursor cursor = new PageCursor(inventoryOfSize(3));

    cursor.apply(page(new PageNumber(7), new MenuAction[3], true, false));

    assertEquals(new PageNumber(7), cursor.page());
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
