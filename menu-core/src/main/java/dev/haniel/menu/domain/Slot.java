package dev.haniel.menu.domain;

/**
 * A validated slot index within a menu.
 *
 * @param value the index; always within the owning menu's bounds
 */
public record Slot(int value) {

  /**
   * Creates a slot, validating it against a menu of the given number of rows.
   *
   * @param value the slot index
   * @param rows the number of rows in the menu
   * @return the validated slot
   * @throws IllegalArgumentException if the index falls outside {@code 0..(rows * 9 - 1)}
   */
  public static Slot of(int value, int rows) {
    int max = rows * 9 - 1;
    if (value < 0 || value > max) {
      throw new IllegalArgumentException("slot " + value + " is out of range 0.." + max);
    }
    return new Slot(value);
  }
}
