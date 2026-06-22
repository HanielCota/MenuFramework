package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.api.component.MenuComponent;
import com.hanielfialho.menuframework.api.layout.MenuRegionCanvas;
import java.util.Objects;
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
   * @return menu layout
   */
  MenuLayout layout();

  /**
   * Assigns a visual item without a click action.
   *
   * @param slot zero-based slot in the top inventory
   * @param icon non-null, non-air icon
   */
  void item(int slot, ItemStack icon);

  /**
   * Assigns a visual item and a click handler.
   *
   * @param slot zero-based slot in the top inventory
   * @param icon non-null, non-air icon
   * @param clickHandler non-null action invoked for a known top click
   */
  void button(int slot, ItemStack icon, MenuClickHandler<S> clickHandler);

  /**
   * Marks a slot as explicitly empty.
   *
   * <p>An explicitly empty slot is not filled by the frame background.
   *
   * @param slot zero-based slot in the top inventory
   */
  void empty(int slot);

  /**
   * Defines the fallback icon for slots that receive no explicit assignment.
   *
   * @param icon non-null, non-air background icon
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

  /**
   * Assigns an item to a required named slot.
   *
   * @param namedSlot named slot declared by the layout
   * @param icon visual item
   */
  default void item(String namedSlot, ItemStack icon) {
    this.item(this.layout().slot(namedSlot), icon);
  }

  /**
   * Assigns a button to a required named slot.
   *
   * @param namedSlot named slot declared by the layout
   * @param icon visual item
   * @param clickHandler button action
   */
  default void button(String namedSlot, ItemStack icon, MenuClickHandler<S> clickHandler) {
    this.button(this.layout().slot(namedSlot), icon, clickHandler);
  }

  /**
   * Marks a required named slot as explicitly empty.
   *
   * @param namedSlot named slot declared by the layout
   */
  default void empty(String namedSlot) {
    this.empty(this.layout().slot(namedSlot));
  }

  /**
   * Binds this canvas to a required named region.
   *
   * @param namedRegion named region declared by the layout
   * @return region-relative facade
   */
  default MenuRegionCanvas<S> region(String namedRegion) {
    return new MenuRegionCanvas<>(this, this.layout().region(namedRegion));
  }

  /**
   * Renders one reusable component into this frame.
   *
   * @param context current render snapshot
   * @param component component to render
   */
  default void component(MenuRenderContext<S> context, MenuComponent<S> component) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(component, "component").render(context, this);
  }
}
