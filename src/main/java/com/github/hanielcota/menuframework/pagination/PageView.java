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
    if (items == null) {
      items = new ItemStack[0];
    } else {
      items = cloneItems(items);
    }
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
    return pageNumber == otherPageNumber
        && totalPages == otherTotalPages
        && Arrays.equals(items, otherItems);
  }

  @Override
  public int hashCode() {
    int result = 31 * pageNumber + totalPages;
    result = 31 * result + Arrays.hashCode(items);
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
