package com.github.hanielcota.menuframework.internal.dispatch;

import com.github.hanielcota.menuframework.api.MenuSession;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public interface MenuEventRouter {

  @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid);

  void closeSession(@NonNull UUID playerUuid);

  boolean dispatchClick(
      @NonNull Player player,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType);

  boolean dispatchDrag(@NonNull Player player, @NonNull InventoryView view);

  boolean dispatchClose(@NonNull Player player, @NonNull InventoryView view);
}
