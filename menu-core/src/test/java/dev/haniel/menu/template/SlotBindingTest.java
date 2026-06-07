package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.MenuAction;
import org.junit.jupiter.api.Test;

class SlotBindingTest {

  @Test
  void exposesSlotAndAction() {
    MenuAction action = context -> {};

    SlotBinding binding = new SlotBinding(4, action);

    assertEquals(4, binding.slot());
    assertSame(action, binding.action());
  }

  @Test
  void rejectsNegativeSlot() {
    assertThrows(IllegalArgumentException.class, () -> new SlotBinding(-1, context -> {}));
  }

  @Test
  void rejectsNullAction() {
    assertThrows(NullPointerException.class, () -> new SlotBinding(0, null));
  }
}
