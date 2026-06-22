package com.hanielfialho.menuframework.api;

import java.util.Objects;
import org.bukkit.event.inventory.InventoryType;

/**
 * Immutable structural description of a menu inventory.
 *
 * <p>This version supports chest inventories containing one through six rows.
 */
public final class MenuLayout {

  /** Number of columns in a chest inventory. */
  public static final int CHEST_COLUMNS = 9;

  /** Minimum supported chest row count. */
  public static final int MIN_CHEST_ROWS = 1;

  /** Maximum supported chest row count. */
  public static final int MAX_CHEST_ROWS = 6;

  private final InventoryType inventoryType;
  private final int rows;
  private final int size;

  private MenuLayout(InventoryType inventoryType, int rows, int size) {
    this.inventoryType = Objects.requireNonNull(inventoryType, "inventoryType");
    this.rows = rows;
    this.size = size;
  }

  /**
   * Creates a validated chest layout.
   *
   * @param rows row count between {@link #MIN_CHEST_ROWS} and {@link #MAX_CHEST_ROWS}, inclusive
   * @return the immutable layout
   * @throws IllegalArgumentException if {@code rows} is outside the range
   */
  public static MenuLayout chest(int rows) {
    if (rows < MIN_CHEST_ROWS || rows > MAX_CHEST_ROWS) {
      throw new IllegalArgumentException(
          "Chest rows must be between " + MIN_CHEST_ROWS + " and " + MAX_CHEST_ROWS + ": " + rows);
    }

    return new MenuLayout(InventoryType.CHEST, rows, rows * CHEST_COLUMNS);
  }

  /**
   * Returns the Bukkit inventory type.
   *
   * @return {@link InventoryType#CHEST}
   */
  public InventoryType inventoryType() {
    return this.inventoryType;
  }

  /**
   * Returns the number of rows.
   *
   * @return row count
   */
  public int rows() {
    return this.rows;
  }

  /**
   * Returns the total number of top-inventory slots.
   *
   * @return inventory size
   */
  public int size() {
    return this.size;
  }

  /**
   * Converts zero-based row and column coordinates into a linear slot.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @return linear slot index
   * @throws IndexOutOfBoundsException if either coordinate is invalid
   */
  public int slot(int row, int column) {
    if (row < 0 || row >= this.rows) {
      throw new IndexOutOfBoundsException(
          "Row must be between 0 and " + (this.rows - 1) + ": " + row);
    }

    if (column < 0 || column >= CHEST_COLUMNS) {
      throw new IndexOutOfBoundsException(
          "Column must be between 0 and " + (CHEST_COLUMNS - 1) + ": " + column);
    }

    return (row * CHEST_COLUMNS) + column;
  }

  /**
   * Returns the zero-based row containing a slot.
   *
   * @param slot linear slot
   * @return zero-based row
   * @throws IndexOutOfBoundsException if {@code slot} is invalid
   */
  public int rowOf(int slot) {
    this.checkSlot(slot);
    return slot / CHEST_COLUMNS;
  }

  /**
   * Returns the zero-based column containing a slot.
   *
   * @param slot linear slot
   * @return zero-based column
   * @throws IndexOutOfBoundsException if {@code slot} is invalid
   */
  public int columnOf(int slot) {
    this.checkSlot(slot);
    return slot % CHEST_COLUMNS;
  }

  /**
   * Tests whether a slot belongs to this layout.
   *
   * @param slot linear slot
   * @return {@code true} when the slot is inside the inventory
   */
  public boolean contains(int slot) {
    return slot >= 0 && slot < this.size;
  }

  /**
   * Validates and returns a slot index.
   *
   * @param slot linear slot
   * @return the supplied slot
   * @throws IndexOutOfBoundsException if the slot does not belong to this layout
   */
  public int checkSlot(int slot) {
    if (!this.contains(slot)) {
      throw new IndexOutOfBoundsException(
          "Slot must be between 0 and " + (this.size - 1) + ": " + slot);
    }

    return slot;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof MenuLayout other)) {
      return false;
    }

    return this.rows == other.rows
        && this.size == other.size
        && this.inventoryType == other.inventoryType;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(this.inventoryType, this.rows, this.size);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "MenuLayout{"
        + "inventoryType="
        + this.inventoryType
        + ", rows="
        + this.rows
        + ", size="
        + this.size
        + '}';
  }
}
