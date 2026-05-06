package com.github.hanielcota.menuframework.core.profile;

import java.util.UUID;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.NonNull;

/**
 * Service for applying player profile data to skull meta items. Abstracts Bukkit player profile
 * APIs to enable testability.
 */
public interface PlayerProfileService {

  /** Applies a player's UUID as the skull owner. */
  void applyPlayerUuid(@NonNull SkullMeta meta, @NonNull UUID playerUuid);

  /** Applies a base64 texture to the skull meta. */
  void applyBase64Texture(@NonNull SkullMeta meta, @NonNull String base64Texture);
}
