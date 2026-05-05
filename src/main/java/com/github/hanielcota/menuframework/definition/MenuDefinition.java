package com.github.hanielcota.menuframework.definition;

import com.github.hanielcota.menuframework.api.MenuFeature;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record MenuDefinition(
    @NonNull String id,
    @NonNull InventoryType type,
    int size,
    @NonNull Component title,
    @NonNull Int2ObjectMap<SlotDefinition> slots,
    @Nullable ItemTemplate fillItem,
    @NonNull PaginationConfig pagination,
    @NonNull List<MenuFeature> features,
    boolean blockPlayerInventoryClicks,
    boolean blockShiftClick) {

  private static final int MIN_CHEST_SIZE = 9;
  private static final int MAX_CHEST_SIZE = 54;

  public MenuDefinition {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(slots, "slots");
    Objects.requireNonNull(pagination, "pagination");
    Objects.requireNonNull(features, "features");

    if (type == InventoryType.CHEST
        && (size < MIN_CHEST_SIZE || size > MAX_CHEST_SIZE || size % MIN_CHEST_SIZE != 0)) {
      throw new IllegalArgumentException(
          "Chest inventory size must be a multiple of "
              + MIN_CHEST_SIZE
              + " between "
              + MIN_CHEST_SIZE
              + " and "
              + MAX_CHEST_SIZE
              + ", got: "
              + size);
    }

    int maxSlots = type == InventoryType.CHEST ? size : type.getDefaultSize();
    for (var entry : slots.int2ObjectEntrySet()) {
      int slot = entry.getIntKey();

      if (slot < 0 || slot >= maxSlots) {
        throw new IllegalArgumentException(
            "Slot " + slot + " out of bounds for inventory with " + maxSlots + " slots");
      }
    }
    validateSlots(pagination.contentSlots(), maxSlots, "pagination content");
    validateSlots(pagination.navigationSlots(), maxSlots, "pagination navigation");

    slots = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(slots));
    features = List.copyOf(features);
  }

  private static void validateSlots(
      @NonNull List<Integer> slots, int maxSlots, @NonNull String label) {
    for (Integer slotObj : slots) {
      if (slotObj == null) {
        throw new IllegalArgumentException("Null slot found in " + label + " slots");
      }
      int slot = slotObj;
      if (slot < 0 || slot >= maxSlots) {
        throw new IllegalArgumentException(
            "Invalid " + label + " slot " + slot + " for inventory with " + maxSlots + " slots");
      }
    }
  }
}
