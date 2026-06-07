package dev.haniel.menu.paper.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.paper.holder.ClickableHolder;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

class MenuListenerTest {

  @Test
  void cancelsDragWhenTopInventoryIsMenu() {
    InventoryDragEvent event = mock(InventoryDragEvent.class);
    Inventory inventory = mock(Inventory.class);

    when(event.getInventory()).thenReturn(inventory);
    when(inventory.getHolder()).thenReturn(mock(ClickableHolder.class));

    new MenuListener().onDrag(event);

    verify(event).setCancelled(true);
  }

  @Test
  void ignoresDragWhenTopInventoryIsNotMenu() {
    InventoryDragEvent event = mock(InventoryDragEvent.class);
    Inventory inventory = mock(Inventory.class);

    when(event.getInventory()).thenReturn(inventory);

    new MenuListener().onDrag(event);

    verify(event, never()).setCancelled(true);
  }
}
