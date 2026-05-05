package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/**
 * Cancels clicks in the top inventory (menu area).
 */
public final class TopInventoryRule implements InteractionRule {

  @Override
  public boolean shouldCancel(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return rawSlot < view.getTopInventory().getSize();
  }
}
