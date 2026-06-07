package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.action.MenuAction;
import org.junit.jupiter.api.Test;

class MenuTemplateTest {

  @Test
  void reportsSlotCount() {
    MenuTemplate<String> template = new MenuTemplate<>(new Object[9], new SlotBinding[0]);

    assertEquals(9, template.size());
  }

  @Test
  void findsVisualAtSetSlot() {
    Object[] icons = new Object[] {"first", null, "third"};

    MenuTemplate<String> template = new MenuTemplate<>(icons, new SlotBinding[0]);

    assertEquals("third", template.iconAt(2).orElseThrow());
  }

  @Test
  void returnsEmptyForUnsetVisual() {
    MenuTemplate<String> template = new MenuTemplate<>(new Object[] {null, "set"}, new SlotBinding[0]);

    assertTrue(template.iconAt(0).isEmpty());
  }

  @Test
  void returnsEmptyForVisualOutOfBounds() {
    MenuTemplate<String> template = new MenuTemplate<>(new Object[3], new SlotBinding[0]);

    assertTrue(template.iconAt(-1).isEmpty());
    assertTrue(template.iconAt(3).isEmpty());
  }

  @Test
  void findsActionBoundToSlot() {
    MenuAction action = context -> {};
    SlotBinding[] bindings = {new SlotBinding(2, action)};

    MenuTemplate<String> template = new MenuTemplate<>(new Object[9], bindings);

    assertSame(action, template.actionAt(2).orElseThrow());
  }

  @Test
  void returnsEmptyForUnboundSlot() {
    SlotBinding[] bindings = {new SlotBinding(2, context -> {})};

    MenuTemplate<String> template = new MenuTemplate<>(new Object[9], bindings);

    assertTrue(template.actionAt(1).isEmpty());
  }

  @Test
  void returnsEmptyForActionOutOfBounds() {
    MenuTemplate<String> template = new MenuTemplate<>(new Object[3], new SlotBinding[0]);

    assertTrue(template.actionAt(-1).isEmpty());
    assertTrue(template.actionAt(3).isEmpty());
  }

  @Test
  void copiesVisualsDefensively() {
    Object[] icons = new Object[] {"original"};
    MenuTemplate<String> template = new MenuTemplate<>(icons, new SlotBinding[0]);

    icons[0] = "mutated";

    assertEquals("original", template.iconAt(0).orElseThrow());
  }
}
