package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.pagination.PageView;
import java.util.Objects;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public final class PageApplier {

  public static void apply(@NonNull InventoryView view, @NonNull PageView pageView, int maxSlots) {
    Objects.requireNonNull(view, "view");
    Objects.requireNonNull(pageView, "pageView");
    if (maxSlots < 0) {
      throw new IllegalArgumentException("maxSlots cannot be negative: " + maxSlots);
    }
    ItemStack[] items = pageView.items();
    java.util.Objects.requireNonNull(items, "pageView.items()");
    var topInv = view.getTopInventory();
    java.util.Objects.requireNonNull(topInv, "topInventory is null (view may be closed)");
    int inventorySize = topInv.getSize();

    for (int slotIndex = 0; slotIndex < maxSlots && slotIndex < inventorySize; slotIndex++) {
      ItemStack item = slotIndex < items.length ? items[slotIndex] : null;
      topInv.setItem(slotIndex, item);
    }
  }
}
