package com.hanielfialho.menuframework.api;

import java.util.Objects;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

/**
 * Immutable snapshot of the relevant properties of a menu-button click.
 *
 * <p>The original Bukkit event is deliberately not exposed, preventing menu code from mutating the
 * inventory view unsafely while the event is being processed.
 *
 * @param rawSlot raw view slot, always inside the top inventory
 * @param clickType platform click type
 * @param action platform-estimated inventory action
 * @param hotbarButton hotbar index or {@link #NO_HOTBAR_BUTTON}
 */
public record MenuClick(
    int rawSlot, ClickType clickType, InventoryAction action, int hotbarButton) {

  /** Value used when the click does not reference a hotbar button. */
  public static final int NO_HOTBAR_BUTTON = -1;

  /**
   * Validates and creates a click snapshot.
   *
   * @throws IllegalArgumentException if either numeric index is invalid
   * @throws NullPointerException if {@code clickType} or {@code action} is {@code null}
   */
  public MenuClick {
    if (rawSlot < 0) {
      throw new IllegalArgumentException("rawSlot must be >= 0: " + rawSlot);
    }

    Objects.requireNonNull(clickType, "clickType");
    Objects.requireNonNull(action, "action");

    if (hotbarButton < NO_HOTBAR_BUTTON || hotbarButton > 8) {
      throw new IllegalArgumentException("hotbarButton must be between -1 and 8: " + hotbarButton);
    }
  }

  /**
   * Returns whether this click references a hotbar position.
   *
   * @return {@code true} for indexes zero through eight
   */
  public boolean usesHotbarButton() {
    return this.hotbarButton >= 0;
  }
}
