package dev.haniel.menu.paper.reactive;

import org.bukkit.inventory.InventoryHolder;

/**
 * A view that holds live references (bound states, scheduled flushes) needing teardown.
 *
 * <p>The close listener calls {@link #close()} on {@link org.bukkit.event.inventory
 * .InventoryCloseEvent} so a closed view leaves no references behind.
 */
public interface ReactiveView extends InventoryHolder {

  /** Unbinds all state and cancels any pending flush, making the view garbage-collectable. */
  void close();
}
