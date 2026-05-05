package com.github.hanielcota.menuframework.core.server;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;

/**
 * Abstraction over Bukkit server operations for testability.
 */
public interface ServerAccess {

  boolean isPrimaryThread();

  @NonNull Optional<@NonNull Player> findOnlinePlayer(@NonNull UUID playerUuid);

  @NonNull Inventory createInventory(@NonNull Player owner, @NonNull MenuDefinition definition);
}
