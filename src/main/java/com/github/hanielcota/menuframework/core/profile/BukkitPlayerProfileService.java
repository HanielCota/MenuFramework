package com.github.hanielcota.menuframework.core.profile;

import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.NonNull;

/**
 * Bukkit implementation of {@link PlayerProfileService}.
 */
public record BukkitPlayerProfileService() implements PlayerProfileService {

  private static final String TEXTURES_PROPERTY_KEY = "textures";

  @Override
  public void applyPlayerUuid(@NonNull SkullMeta meta, @NonNull UUID playerUuid) {
    var offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
    meta.setOwningPlayer(offlinePlayer);
  }

  @Override
  public void applyBase64Texture(@NonNull SkullMeta meta, @NonNull String base64Texture) {
    var profile = Bukkit.createProfile(UUID.randomUUID());
    profile.setProperty(new ProfileProperty(TEXTURES_PROPERTY_KEY, base64Texture));
    meta.setPlayerProfile(profile);
  }
}
