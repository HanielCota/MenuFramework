package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/** Cancels shift clicks if the menu is configured to block them. */
public final class ShiftClickRule implements InteractionRule {

  @Override
  public boolean shouldCancel(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return clickType.isShiftClick() && definition.blockShiftClick();
  }
}
