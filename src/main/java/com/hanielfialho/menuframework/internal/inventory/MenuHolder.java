package com.hanielfialho.menuframework.internal.inventory;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

/** Identifies an inventory as belonging to one framework runtime and session. */
public final class MenuHolder implements InventoryHolder {

  private final UUID runtimeId;
  private final UUID sessionId;
  private final InteractionPolicy interactionPolicy;
  private final Inventory inventory;

  public MenuHolder(
      UUID runtimeId,
      UUID sessionId,
      MenuLayout layout,
      Component title,
      InteractionPolicy interactionPolicy) {
    this.runtimeId = Objects.requireNonNull(runtimeId, "runtimeId");
    this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(title, "title");
    this.interactionPolicy = Objects.requireNonNull(interactionPolicy, "interactionPolicy");

    if (layout.inventoryType() != InventoryType.CHEST) {
      throw new IllegalArgumentException("Unsupported inventory type: " + layout.inventoryType());
    }

    this.inventory = Bukkit.createInventory(this, layout.size(), title);
    MenuViewAccess.track(this);
  }

  public UUID runtimeId() {
    return this.runtimeId;
  }

  public UUID sessionId() {
    return this.sessionId;
  }

  public InteractionPolicy interactionPolicy() {
    return this.interactionPolicy;
  }

  @Override
  public @NonNull Inventory getInventory() {
    return this.inventory;
  }
}
