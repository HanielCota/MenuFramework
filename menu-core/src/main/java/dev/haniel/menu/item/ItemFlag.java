package dev.haniel.menu.item;

/**
 * A platform-neutral tooltip flag that hides part of an item's generated tooltip.
 *
 * <p>Mirrors the Bukkit {@code ItemFlag} set without depending on it, so the domain stays testable
 * without a server. The Paper layer maps each constant to its Bukkit counterpart at render time.
 */
public enum ItemFlag {

  /** Hides enchantments (real or glint-override) from the tooltip. */
  HIDE_ENCHANTS,

  /** Hides attribute modifiers (armor, damage, speed) from the tooltip. */
  HIDE_ATTRIBUTES,

  /** Hides the {@code Unbreakable} line from the tooltip. */
  HIDE_UNBREAKABLE,

  /** Hides what the item can break in adventure mode. */
  HIDE_DESTROYS,

  /** Hides what the item can be placed on in adventure mode. */
  HIDE_PLACED_ON,

  /** Hides dye information on coloured leather armor. */
  HIDE_DYE,

  /** Hides the armor trim shown on trimmed armor. */
  HIDE_ARMOR_TRIM,

  /** Hides extra tooltip lines such as potion effects, book contents or banner patterns. */
  HIDE_ADDITIONAL_TOOLTIP
}
