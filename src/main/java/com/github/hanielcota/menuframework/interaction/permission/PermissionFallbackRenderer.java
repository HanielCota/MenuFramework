package com.github.hanielcota.menuframework.interaction.permission;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Renders the permission fallback template when a player lacks permission for a slot. */
public final class PermissionFallbackRenderer {

  private final MenuService menuService;

  public PermissionFallbackRenderer(@NonNull MenuService menuService) {
    this.menuService = menuService;
  }

  /** Updates the slot with the fallback template if one is configured. */
  public void renderFallback(@NonNull Player player, int rawSlot, @NonNull SlotDefinition slotDefinition) {
    var fallback = slotDefinition.permissionFallbackTemplate();
    if (fallback == null) {
      return;
    }
    var session = menuService.getSession(player.getUniqueId()).orElse(null);
    if (session == null) return;
    session.updateSlot(rawSlot, fallback);
  }
}
