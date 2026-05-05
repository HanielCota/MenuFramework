package com.github.hanielcota.menuframework.internal.server;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;

public interface ServerAccess {

  boolean isPrimaryThread();

  @NonNull Optional<@NonNull Player> findOnlinePlayer(@NonNull UUID playerUuid);

  @NonNull Inventory createInventory(@NonNull Player owner, @NonNull MenuDefinition definition);
}
