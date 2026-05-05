package com.github.hanielcota.menuframework.api;

import java.util.Objects;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

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
  public static @NonNull MenuFeature refreshInterval(long ticks) {
    if (ticks <= 0) {
      throw new IllegalArgumentException("refresh interval must be > 0, got: " + ticks);
    }
    return new RefreshIntervalFeature(ticks);
  }

  private record SoundOnOpenFeature(@NonNull Sound sound) implements MenuFeature {
    @Override
    public void onOpen(@NonNull MenuSession session) {
      var viewer = session.view().getPlayer();

      if (viewer instanceof Player player && player.isOnline()) {
        player.playSound(sound);
      }
    }
  }

  private record SoundOnClickFeature(@NonNull Sound sound) implements MenuFeature {
    @Override
    public void onClick(@NonNull ClickContext context) {
      var player = context.player();
      if (player.isOnline()) {
        player.playSound(sound);
      }
    }
  }

  public record RefreshIntervalFeature(long refreshIntervalTicks)
      implements RefreshingMenuFeature {}
}
