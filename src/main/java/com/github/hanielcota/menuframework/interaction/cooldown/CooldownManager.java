package com.github.hanielcota.menuframework.interaction.cooldown;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Manages global and per-slot click cooldowns for menu interactions. */
public final class CooldownManager {

  private static final long DEFAULT_COOLDOWN_MS = 100;
  private static final long COOLDOWN_CACHE_SECONDS = 1;

  private final Cache<UUID, Long> globalCooldowns =
      Caffeine.newBuilder().expireAfterWrite(COOLDOWN_CACHE_SECONDS, TimeUnit.SECONDS).build();

  private final Cache<String, Long> slotCooldowns =
      Caffeine.newBuilder().expireAfterWrite(COOLDOWN_CACHE_SECONDS, TimeUnit.SECONDS).build();

  /**
   * Checks if the player is on cooldown for the given slot. Also records the cooldown if not
   * already on cooldown.
   */
  public synchronized boolean isOnCooldown(@NonNull Player player, @NonNull SlotDefinition slotDefinition) {
    var now = System.currentTimeMillis();
    var uuid = player.getUniqueId();

    boolean onGlobal = isOnGlobalCooldown(uuid, now);
    boolean onSlot = isOnSlotCooldown(uuid, slotDefinition, now);

    if (!onGlobal && !onSlot) {
      globalCooldowns.put(uuid, now);
      registerSlotCooldown(uuid, slotDefinition, now);
    }

    return onGlobal || onSlot;
  }

  private boolean isOnGlobalCooldown(@NonNull UUID uuid, long now) {
    var lastGlobalClick = globalCooldowns.getIfPresent(uuid);
    return lastGlobalClick != null && (now - lastGlobalClick) < DEFAULT_COOLDOWN_MS;
  }

  private boolean isOnSlotCooldown(
      @NonNull UUID uuid, @NonNull SlotDefinition slotDefinition, long now) {
    long slotCooldown = slotDefinition.cooldownTicks() * 50; // Convert ticks to ms
    if (slotCooldown <= 0) {
      return false;
    }

    String slotKey = uuid + ":" + slotDefinition.slot();
    var lastSlotClick = slotCooldowns.getIfPresent(slotKey);
    return lastSlotClick != null && (now - lastSlotClick) < slotCooldown;
  }

  private void registerSlotCooldown(
      @NonNull UUID uuid, @NonNull SlotDefinition slotDefinition, long now) {
    long slotCooldown = slotDefinition.cooldownTicks() * 50;
    if (slotCooldown > 0) {
      String slotKey = uuid + ":" + slotDefinition.slot();
      slotCooldowns.put(slotKey, now);
    }
  }
}
