package com.github.hanielcota.menuframework.internal.session;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public interface InteractiveMenuSession {

  boolean handleClick(int rawSlot, @NonNull ClickType clickType);

  boolean isSameView(@NonNull InventoryView other);

  void disposeImmediately();
}
