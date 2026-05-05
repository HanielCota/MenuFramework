package com.github.hanielcota.menuframework.feature.internal;

import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuSession;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Plays a sound when the menu is opened. */
public record SoundOnOpenFeature(@NonNull Sound sound) implements MenuFeature {

  @Override
  public void onOpen(@NonNull MenuSession session) {
    var viewer = session.view().getPlayer();
    if (viewer instanceof Player player && player.isOnline()) {
      player.playSound(sound);
    }
  }
}
