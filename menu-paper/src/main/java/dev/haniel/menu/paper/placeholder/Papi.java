package dev.haniel.menu.paper.placeholder;

import org.bukkit.entity.Player;

/**
 * The single point that references PlaceholderAPI types, isolated so the class loads only when the
 * plugin is present. Callers must check availability first; see {@link PapiPlaceholders}.
 */
final class Papi {

  private Papi() {}

  static String apply(Player player, String text) {
    return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
  }
}
