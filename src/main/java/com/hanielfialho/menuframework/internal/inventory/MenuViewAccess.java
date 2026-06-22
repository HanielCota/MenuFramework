package com.hanielfialho.menuframework.internal.inventory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Operações de identidade sobre a view atualmente aberta pelo jogador. */
public final class MenuViewAccess {

  private static final String MOCK_BUKKIT_UNIMPLEMENTED_OPERATION =
      "org.mockbukkit.mockbukkit.exception.UnimplementedOperationException";
  private static final Map<Inventory, MenuHolder> TRACKED_HOLDERS = new ConcurrentHashMap<>();

  private MenuViewAccess() {}

  public static boolean isSessionInventoryOpen(Player viewer, UUID sessionId) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(sessionId, "sessionId");

    return belongsTo(viewer.getOpenInventory().getTopInventory(), sessionId);
  }

  public static boolean belongsTo(Inventory inventory, UUID sessionId) {
    Objects.requireNonNull(inventory, "inventory");
    Objects.requireNonNull(sessionId, "sessionId");

    MenuHolder holder = holderOf(inventory);

    return holder != null && holder.sessionId().equals(sessionId);
  }

  public static void track(MenuHolder holder) {
    Objects.requireNonNull(holder, "holder");
    TRACKED_HOLDERS.put(holder.getInventory(), holder);
  }

  public static void untrack(MenuHolder holder) {
    Objects.requireNonNull(holder, "holder");
    TRACKED_HOLDERS.remove(holder.getInventory(), holder);
  }

  public static MenuHolder holderOf(Inventory inventory) {
    Objects.requireNonNull(inventory, "inventory");

    try {
      InventoryHolder holder = inventory.getHolder(false);

      if (holder instanceof MenuHolder menuHolder) {
        return menuHolder;
      }
    } catch (RuntimeException failure) {
      if (!isMockBukkitUnimplementedOperation(failure)) {
        throw failure;
      }
      // Some lightweight mocks do not implement InventoryHolder lookup via getHolder(false).
    }

    return TRACKED_HOLDERS.get(inventory);
  }

  private static boolean isMockBukkitUnimplementedOperation(RuntimeException failure) {
    try {
      return Class.forName(MOCK_BUKKIT_UNIMPLEMENTED_OPERATION).isInstance(failure);
    } catch (ClassNotFoundException notRunningUnderMockBukkit) {
      return false;
    }
  }
}
