package dev.haniel.menu.paper.render.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.action.MenuAction;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class OverlayTest {

  @Test
  void exposesVisualsAndActions() {
    ItemStack item = mock(ItemStack.class);
    MenuAction action = ignored -> {};
    Overlay overlay = new Overlay(Map.of(3, item), Map.of(3, action));

    assertEquals(item, overlay.visuals().get(3));
    assertEquals(action, overlay.actions().get(3));
  }

  @Test
  void copiesVisualsDefensively() {
    Map<Integer, ItemStack> source = new HashMap<>();
    source.put(0, mock(ItemStack.class));
    Overlay overlay = new Overlay(source, Map.of());

    source.put(1, mock(ItemStack.class));

    assertEquals(1, overlay.visuals().size());
  }

  @Test
  void copiesActionsDefensively() {
    Map<Integer, MenuAction> source = new HashMap<>();
    source.put(0, ignored -> {});
    Overlay overlay = new Overlay(Map.of(), source);

    source.put(1, ignored -> {});

    assertEquals(1, overlay.actions().size());
  }

  @Test
  void exposedVisualsAreUnmodifiable() {
    Overlay overlay = new Overlay(Map.of(0, mock(ItemStack.class)), Map.of());

    assertThrows(
        UnsupportedOperationException.class,
        () -> overlay.visuals().put(1, mock(ItemStack.class)));
  }

  @Test
  void supportsEmptyMaps() {
    Overlay overlay = new Overlay(Map.of(), Map.of());

    assertTrue(overlay.visuals().isEmpty());
    assertTrue(overlay.actions().isEmpty());
  }
}
