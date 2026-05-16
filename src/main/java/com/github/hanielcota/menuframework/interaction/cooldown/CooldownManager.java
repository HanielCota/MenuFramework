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
  // Must be >= the longest slot cooldown a developer could configure (in seconds).
  // 6000 ticks = 300 s; raise this if you allow longer cooldowns.
  private static final long MAX_SLOT_COOLDOWN_SECONDS = 300;

  private final Cache<UUID, Long> globalCooldowns =
      Caffeine.newBuilder().expireAfterWrite(COOLDOWN_CACHE_SECONDS, TimeUnit.SECONDS).build();

  // Values are absolute expiry timestamps (ms), not last-click timestamps.
  // TTL must cover the full cooldown duration so Caffeine does not evict entries early.
  private final Cache<String, Long> slotCooldowns =
      Caffeine.newBuilder().expireAfterWrite(MAX_SLOT_COOLDOWN_SECONDS, TimeUnit.SECONDS).build();

  /**
   * Checks if the player is on cooldown for the given slot. Also records the cooldown if not
   * already on cooldown.
   */
  public synchronized boolean isOnCooldown(
      @NonNull Player player, @NonNull SlotDefinition slotDefinition) {
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
    long slotCooldown = slotDefinition.cooldownTicks() * 50L;
    if (slotCooldown <= 0) {
      return false;
    }

    String slotKey = uuid + ":" + slotDefinition.slot();
    var expiry = slotCooldowns.getIfPresent(slotKey);
    return expiry != null && now < expiry;
  }

  private void registerSlotCooldown(
      @NonNull UUID uuid, @NonNull SlotDefinition slotDefinition, long now) {
    long slotCooldown = slotDefinition.cooldownTicks() * 50L;
    if (slotCooldown > 0) {
      String slotKey = uuid + ":" + slotDefinition.slot();
      slotCooldowns.put(slotKey, now + slotCooldown);
    }
  }
}
