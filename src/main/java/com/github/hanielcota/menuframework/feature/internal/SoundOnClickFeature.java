package com.github.hanielcota.menuframework.feature.internal;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuFeature;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.NonNull;

/** Plays a sound when a slot is clicked. */
public record SoundOnClickFeature(@NonNull Sound sound) implements MenuFeature {

  @Override
  public void onClick(@NonNull ClickContext context) {
    var player = context.player();
    if (player.isOnline()) {
      player.playSound(sound);
    }
  }
}
