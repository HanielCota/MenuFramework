package com.hanielfialho.menuframework.api.feedback;

import java.util.Objects;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

/**
 * Immutable Bukkit sound parameters associated with a feedback signal.
 *
 * @param sound Bukkit sound
 * @param category sound category
 * @param volume non-negative volume
 * @param pitch positive pitch
 */
public record MenuSoundEffect(Sound sound, SoundCategory category, float volume, float pitch) {

  /** Validates sound parameters. */
  public MenuSoundEffect {
    Objects.requireNonNull(sound, "sound");
    Objects.requireNonNull(category, "category");

    if (!Float.isFinite(volume) || volume < 0.0F) {
      throw new IllegalArgumentException("volume must be finite and >= 0: " + volume);
    }

    if (!Float.isFinite(pitch) || pitch <= 0.0F) {
      throw new IllegalArgumentException("pitch must be finite and > 0: " + pitch);
    }
  }

  /**
   * Creates a master-category effect.
   *
   * @param sound Bukkit sound
   * @param volume non-negative volume
   * @param pitch positive pitch
   * @return validated effect
   */
  public static MenuSoundEffect master(Sound sound, float volume, float pitch) {
    return new MenuSoundEffect(sound, SoundCategory.MASTER, volume, pitch);
  }
}
