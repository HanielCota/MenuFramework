package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/**
 * Rule that determines whether an unhandled click should be cancelled.
 */
@FunctionalInterface
public interface InteractionRule {

  /**
   * Returns true if the click should be cancelled.
   */
  boolean shouldCancel(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType);
}
