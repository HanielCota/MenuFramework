package com.github.hanielcota.menuframework.internal.dispatch;

import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.internal.registry.SessionRegistry;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class DefaultMenuEventRouter implements MenuEventRouter {

  @NonNull private final SessionRegistry sessionRegistry;
  @NonNull private final ClickDispatcher clickDispatcher;

  public DefaultMenuEventRouter(
      @NonNull SessionRegistry sessionRegistry,
      @NonNull ClickDispatcher clickDispatcher) {
    this.sessionRegistry = sessionRegistry;
    this.clickDispatcher = clickDispatcher;
  }

  @Override
  public @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid) {
    return sessionRegistry.getSession(playerUuid);
  }

  @Override
  public void closeSession(@NonNull UUID playerUuid) {
    sessionRegistry.closeSession(playerUuid);
  }

  @Override
  public boolean dispatchClick(
      @NonNull Player player,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return clickDispatcher.dispatchClick(player, view, rawSlot, clickType);
  }

  @Override
  public boolean dispatchDrag(@NonNull Player player, @NonNull InventoryView view) {
    return clickDispatcher.dispatchDrag(player, view);
  }

  @Override
  public boolean dispatchClose(@NonNull Player player, @NonNull InventoryView view) {
    return clickDispatcher.dispatchClose(player, view);
  }
}
