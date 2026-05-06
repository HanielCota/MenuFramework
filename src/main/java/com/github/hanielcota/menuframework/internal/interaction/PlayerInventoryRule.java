package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/** Cancels clicks in the player inventory if the menu is configured to block them. */
public final class PlayerInventoryRule implements InteractionRule {

  @Override
  public boolean shouldCancel(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return definition.blockPlayerInventoryClicks();
  }
}
