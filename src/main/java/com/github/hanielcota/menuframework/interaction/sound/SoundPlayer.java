package com.github.hanielcota.menuframework.interaction.sound;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Plays click sounds for menu slot interactions. */
public final class SoundPlayer {

  private static final float DEFAULT_VOLUME = 1.0f;
  private static final float DEFAULT_PITCH = 1.0f;

  /** Plays the configured click sound for the given slot, if any. */
  public void playClickSound(@NonNull Player player, @NonNull MenuDefinition definition, int slot, int inventorySize) {
    if (slot < 0 || slot >= inventorySize) return;
    var slotDef = definition.slots().get(slot);
    if (slotDef == null) return;
    var template = slotDef.template();
    if (template == null) return;

    if (!player.isOnline()) return;

    Sound sound = template.clickSound();
    if (sound == null) return;

    Location location = player.getLocation();
    player.playSound(location, sound, DEFAULT_VOLUME, DEFAULT_PITCH);
  }
}
