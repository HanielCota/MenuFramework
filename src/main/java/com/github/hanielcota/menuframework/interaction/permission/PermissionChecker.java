package com.github.hanielcota.menuframework.interaction.permission;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Checks whether a player has the required permission for a slot. */
public final class PermissionChecker {

  /**
   * Returns true if the player has the required permission for the slot, or if no permission is
   * required.
   */
  public boolean hasPermission(@NonNull Player player, @NonNull SlotDefinition slotDefinition) {
    String permission = slotDefinition.requiredPermission();
    if (permission == null || permission.isEmpty()) {
      return true;
    }
    return player.hasPermission(permission);
  }
}
