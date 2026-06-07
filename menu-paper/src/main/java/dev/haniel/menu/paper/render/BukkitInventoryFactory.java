package dev.haniel.menu.paper.render;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Bukkit-backed inventory factory. */
public final class BukkitInventoryFactory implements InventoryFactory {

  @Override
  public Inventory create(InventoryHolder holder, int size, Component title) {
    return Bukkit.createInventory(holder, size, title);
  }
}
