package com.github.hanielcota.menuframework.api;

import com.github.hanielcota.menuframework.feature.internal.SoundOnClickFeature;
import com.github.hanielcota.menuframework.feature.internal.SoundOnOpenFeature;
import java.util.Objects;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.NonNull;

/**
 * Factory methods for built-in {@link MenuFeature} implementations.
 *
 * @see MenuFeature
 * @see RefreshingMenuFeature
 */
public final class MenuFeatures {

  private MenuFeatures() {}

  public static @NonNull MenuFeature soundOnOpen(@NonNull Sound sound) {
    return new SoundOnOpenFeature(Objects.requireNonNull(sound, "sound"));
  }

  public static @NonNull MenuFeature soundOnClick(@NonNull Sound sound) {
    return new SoundOnClickFeature(Objects.requireNonNull(sound, "sound"));
  }

  /**
   * Refreshes the menu at a fixed interval. Useful for live counters, clocks, etc.
   *
   * <p>The task is automatically canceled when the menu closes.
   */
  public static void refreshInterval(long ticks) {
    if (ticks <= 0) {
      throw new IllegalArgumentException("ticks must be positive: " + ticks);
    }
  }
}
