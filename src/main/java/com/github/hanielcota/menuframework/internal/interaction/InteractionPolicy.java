package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.List;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

/** Determines whether unhandled clicks should be cancelled using a composable set of rules. */
public final class InteractionPolicy {

  private final List<InteractionRule> rules;

  public InteractionPolicy() {
    this(
        List.of(
            new NegativeSlotRule(),
            new TopInventoryRule(),
            new ShiftClickRule(),
            new PlayerInventoryRule()));
  }

  public InteractionPolicy(@NonNull List<InteractionRule> rules) {
    this.rules = List.copyOf(rules);
  }

  public boolean shouldCancelUnhandledClick(
      @NonNull MenuDefinition definition,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    for (var rule : rules) {
      if (rule.shouldCancel(definition, view, rawSlot, clickType)) {
        return true;
      }
    }
    return false;
  }
}
