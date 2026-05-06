package com.github.hanielcota.menuframework.pagination;

import java.util.Arrays;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record PageView(int pageNumber, @Nullable ItemStack[] items, int totalPages) {

  public PageView {
    if (pageNumber < 0) {
      throw new IllegalArgumentException("pageNumber cannot be negative: " + pageNumber);
    }
    if (totalPages < 1) {
      throw new IllegalArgumentException("totalPages must be >= 1, got: " + totalPages);
    }
    items = items == null ? new ItemStack[0] : cloneItems(items);
  }

  private static @NonNull ItemStack[] cloneItems(@Nullable ItemStack[] source) {
    if (source == null) return new ItemStack[0];

    ItemStack[] copy = new ItemStack[source.length];
    for (int slotIndex = 0; slotIndex < source.length; slotIndex++) {
      ItemStack item = source[slotIndex];
      copy[slotIndex] = item == null ? null : item.clone();
    }
    return copy;
  }

  private static boolean areItemsEqual(@Nullable ItemStack a, @Nullable ItemStack b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    return a.isSimilar(b);
  }

  public @Nullable ItemStack get(int slot) {
    if (slot < 0 || slot >= items.length) return null;
    ItemStack item = items[slot];
    return item == null ? null : item.clone();
  }

  @Override
  public @NonNull ItemStack[] items() {
    return cloneItems(items);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PageView(int otherPageNumber, ItemStack[] otherItems, int otherTotalPages)))
      return false;
    if (pageNumber != otherPageNumber || totalPages != otherTotalPages) return false;
    if (items.length != otherItems.length) return false;
    for (int i = 0; i < items.length; i++) {
      if (!areItemsEqual(items[i], otherItems[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 31 * pageNumber + totalPages;
    for (ItemStack item : items) {
      result = 31 * result + (item == null ? 0 : item.hashCode());
    }
    return result;
  }

  @Override
  public @NonNull String toString() {
    return "PageView["
        + "pageNumber="
        + pageNumber
        + ", items="
        + Arrays.toString(items)
        + ", totalPages="
        + totalPages
        + ']';
  }
}
