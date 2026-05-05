package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Objects;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class InteractionPolicy {

  public boolean shouldCancelUnhandledClick(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(view, "view");
    Objects.requireNonNull(clickType, "clickType");
    if (rawSlot < 0) return true;
    if (rawSlot < view.getTopInventory().getSize()) return true;
    if (clickType.isShiftClick()) return definition.blockShiftClick();
    return definition.blockPlayerInventoryClicks();
  }
}
