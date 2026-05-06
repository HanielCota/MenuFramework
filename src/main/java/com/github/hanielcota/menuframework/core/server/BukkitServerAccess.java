package com.github.hanielcota.menuframework.core.server;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;

public record BukkitServerAccess() implements ServerAccess {

  @Override
  public boolean isPrimaryThread() {
    return Bukkit.isPrimaryThread();
  }

  @Override
  public @NonNull Optional<@NonNull Player> findOnlinePlayer(@NonNull UUID playerUuid) {
    Player player = Bukkit.getPlayer(Objects.requireNonNull(playerUuid, "playerUuid"));
    return player == null || !player.isOnline() ? Optional.empty() : Optional.of(player);
  }

  @Override
  public @NonNull Inventory createInventory(@NonNull Player owner, @NonNull MenuDefinition definition) {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(definition, "definition");

    if (definition.type() == InventoryType.CHEST) {
      return Objects.requireNonNull(
          Bukkit.createInventory(owner, definition.size(), definition.title()),
          "Bukkit.createInventory returned null for menu: " + definition.id());
    }
    return Objects.requireNonNull(
        Bukkit.createInventory(owner, definition.type(), definition.title()),
        "Bukkit.createInventory returned null for menu: " + definition.id());
  }
}
