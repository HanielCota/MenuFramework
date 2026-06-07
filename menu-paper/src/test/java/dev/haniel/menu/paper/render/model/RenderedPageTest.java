package dev.haniel.menu.paper.render.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.PageNumber;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class RenderedPageTest {

  @Test
  void exposesPageSlotsAndActions() {
    ItemStack[] slots = {mock(ItemStack.class), null};
    MenuAction[] actions = {ignored -> {}, null};
    RenderedPage page = new RenderedPage(new PageNumber(2), slots, actions, true, false);

    assertEquals(new PageNumber(2), page.page());
    assertArrayEquals(slots, page.slots());
    assertArrayEquals(actions, page.actions());
  }

  @Test
  void carriesNavigationFlags() {
    RenderedPage page = new RenderedPage(PageNumber.first(), new ItemStack[0], new MenuAction[0], true, true);

    assertTrue(page.hasPrevious());
    assertTrue(page.hasNext());
  }

  @Test
  void firstPageWithoutNeighbours() {
    RenderedPage page =
        new RenderedPage(PageNumber.first(), new ItemStack[0], new MenuAction[0], false, false);

    assertFalse(page.hasPrevious());
    assertFalse(page.hasNext());
  }
}
