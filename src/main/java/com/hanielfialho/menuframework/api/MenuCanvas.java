package com.hanielfialho.menuframework.api;

import org.bukkit.inventory.ItemStack;

/**
 * Mutable canvas used to build one complete menu frame.
 *
 * <p>A canvas is valid only during a single render invocation. The framework defensively copies
 * every supplied {@link ItemStack}; subsequent changes to the caller's item do not mutate the
 * frame. Each slot can be assigned at most once per frame.
 *
 * @param <S> session-state type used by button handlers
 */
public interface MenuCanvas<S> {

  /**
   * Returns the layout associated with this frame.
   *
   * @return the menu layout
   */
  MenuLayout layout();

  /**
   * Assigns a visual item without a click action.
   *
   * @param slot zero-based slot in the top inventory
   * @param icon non-null, non-air icon
   * @throws IndexOutOfBoundsException if {@code slot} is outside the layout
   * @throws IllegalStateException if the slot was already assigned
   * @throws NullPointerException if {@code icon} is {@code null}
   * @throws IllegalArgumentException if {@code icon} is air or invalid
   */
  void item(int slot, ItemStack icon);

  /**
   * Assigns a visual item and a click handler.
   *
   * @param slot zero-based slot in the top inventory
   * @param icon non-null, non-air icon
   * @param clickHandler non-null action invoked for a known top click
   * @throws IndexOutOfBoundsException if {@code slot} is outside the layout
   * @throws IllegalStateException if the slot was already assigned
   * @throws NullPointerException if an argument is {@code null}
   * @throws IllegalArgumentException if {@code icon} is air or invalid
   */
  void button(int slot, ItemStack icon, MenuClickHandler<S> clickHandler);

  /**
   * Marks a slot as explicitly empty.
   *
   * <p>An explicitly empty slot is not filled by the frame background.
   *
   * @param slot zero-based slot in the top inventory
   * @throws IndexOutOfBoundsException if {@code slot} is outside the layout
   * @throws IllegalStateException if the slot was already assigned
   */
  void empty(int slot);

  /**
   * Defines the fallback icon for slots that receive no explicit assignment.
   *
   * @param icon non-null, non-air background icon
   * @throws IllegalStateException if a background was already configured
   * @throws NullPointerException if {@code icon} is {@code null}
   * @throws IllegalArgumentException if {@code icon} is air or invalid
   */
  void background(ItemStack icon);

  /**
   * Assigns a visual item using zero-based row and column coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param icon visual item
   */
  default void item(int row, int column, ItemStack icon) {
    this.item(this.layout().slot(row, column), icon);
  }

  /**
   * Assigns a button using zero-based row and column coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param icon visual item
   * @param clickHandler button action
   */
  default void button(int row, int column, ItemStack icon, MenuClickHandler<S> clickHandler) {
    this.button(this.layout().slot(row, column), icon, clickHandler);
  }

  /**
   * Marks a slot identified by zero-based row and column as explicitly empty.
   *
   * @param row zero-based row
   * @param column zero-based column
   */
  default void empty(int row, int column) {
    this.empty(this.layout().slot(row, column));
  }
}
