package dev.haniel.menu.paper.api;

import dev.haniel.menu.item.Icon;
import org.bukkit.Material;

/**
 * Type-safe entry point for building {@link Icon}s in code from a Bukkit {@link Material}.
 *
 * <p>Prefer this over {@link Icon#of(String)} when writing menu code: a {@link Material} is checked
 * by the compiler, so a typo cannot slip through to a runtime render failure. The returned {@link
 * Icon} is the same immutable value object, so {@code named} and {@code describedBy} chain as usual.
 */
public final class Icons {

  private Icons() {}

  /**
   * Creates an icon for the given material with no name or lore.
   *
   * @param material the Bukkit material; never null
   * @return a new icon backed by {@code material.name()}
   */
  public static Icon of(Material material) {
    return Icon.of(material.name());
  }
}
