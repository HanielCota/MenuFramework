package com.github.hanielcota.menuframework.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface MenuOpeningService {

  /**
   * Opens a registered menu for an online player.
   *
   * @return a future completed after the inventory has been created and opened on the server thread
   */
  @NonNull CompletableFuture<MenuSession> open(@NonNull Player player, @NonNull String menuId);

  /**
   * Opens a registered menu for an online player by UUID.
   *
   * @return a future completed after the inventory has been created and opened on the server thread
   */
  @NonNull CompletableFuture<MenuSession> open(@NonNull UUID playerUuid, @NonNull String menuId);
}
