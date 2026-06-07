package dev.haniel.menu.paper.render;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Creates platform inventories for rendered menu views. */
@FunctionalInterface
public interface InventoryFactory {

  /**
   * Creates a new inventory.
   *
   * @param holder the holder to bind
   * @param size the inventory size
   * @param title the inventory title
   * @return the created inventory
   */
  Inventory create(InventoryHolder holder, int size, Component title);
}
