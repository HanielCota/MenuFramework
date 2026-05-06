package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/** Cancels clicks on negative slots (outside inventory). */
public final class NegativeSlotRule implements InteractionRule {

  @Override
  public boolean shouldCancel(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return rawSlot < 0;
  }
}
