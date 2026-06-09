package dev.haniel.menu.paper.reactive;

import java.util.Objects;
import java.util.stream.IntStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Writes a rendered page into an inventory, touching only the slots that actually changed.
 *
 * <p>Keeps the previous snapshot and compares slot by slot, so an unchanged item is left in place
 * and equal items are never re-set. The same {@link Inventory} instance is reused — never
 * recreated.
 */
public final class DiffWriter {

  private final Inventory inventory;
  private ItemStack[] previous;

  /**
   * Starts a writer over the given inventory.
   *
   * @param inventory the inventory to update; never null
   */
  public DiffWriter(Inventory inventory) {
    this.inventory = Objects.requireNonNull(inventory, "inventory");
    this.previous = new ItemStack[inventory.getSize()];
  }

  /**
   * Returns the backing inventory.
   *
   * @return the inventory
   */
  public Inventory inventory() {
    return inventory;
  }

  /**
   * Applies the next slots, setting only those that differ from the previous snapshot.
   *
   * <p>Writes never reach past the inventory's last slot: the range is clamped to the inventory
   * size, so an over- or under-sized array cannot index out of bounds.
   *
   * @param next the item per slot; never null
   */
  public void write(ItemStack[] next) {
    int limit = Math.min(next.length, inventory.getSize());
    IntStream.range(0, limit)
        .filter(slot -> !Objects.equals(previous[slot], next[slot]))
        .forEach(slot -> inventory.setItem(slot, next[slot]));
    IntStream.range(limit, inventory.getSize())
        .filter(slot -> previous[slot] != null)
        .forEach(slot -> inventory.setItem(slot, null));
    previous = snapshot(next);
  }

  private ItemStack[] snapshot(ItemStack[] next) {
    ItemStack[] copy = new ItemStack[inventory.getSize()];
    System.arraycopy(next, 0, copy, 0, Math.min(next.length, copy.length));
    return copy;
  }

  /** Drops the inventory reference so a closed view can be garbage-collected. */
  public void clear() {
    previous = new ItemStack[0];
  }
}
