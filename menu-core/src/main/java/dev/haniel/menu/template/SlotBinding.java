package dev.haniel.menu.template;

import dev.haniel.menu.action.MenuAction;
import java.util.Objects;

/**
 * Binds a slot index to the action fired when that slot is clicked.
 *
 * <p>Array-friendly: the compiler emits one binding per button and the template flattens
 * them into a slot-indexed array for O(1) lookup.
 *
 * @param slot the slot index; must be zero or positive
 * @param action the action to run; never null
 */
public record SlotBinding(int slot, MenuAction action) {

  public SlotBinding {
    if (slot < 0) {
      throw new IllegalArgumentException("slot must be >= 0");
    }
    Objects.requireNonNull(action, "action");
  }
}
