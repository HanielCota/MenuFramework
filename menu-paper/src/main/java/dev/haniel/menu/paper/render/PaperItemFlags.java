package dev.haniel.menu.paper.render;

/**
 * Maps the platform-neutral {@link dev.haniel.menu.item.ItemFlag} to the Bukkit {@code ItemFlag}.
 *
 * <p>An explicit switch rather than {@code valueOf(name())} so a rename on either side fails to
 * compile instead of at render time.
 */
final class PaperItemFlags {

  private PaperItemFlags() {}

  // Bukkit is deprecating some flags in favour of data components, but the ItemFlag API still
  // works and stays the portable way to hide tooltip parts; the server provides it at runtime.
  @SuppressWarnings("deprecation")
  static org.bukkit.inventory.ItemFlag toBukkit(dev.haniel.menu.item.ItemFlag flag) {
    return switch (flag) {
      case HIDE_ENCHANTS -> org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS;
      case HIDE_ATTRIBUTES -> org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;
      case HIDE_UNBREAKABLE -> org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
      case HIDE_DESTROYS -> org.bukkit.inventory.ItemFlag.HIDE_DESTROYS;
      case HIDE_PLACED_ON -> org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON;
      case HIDE_DYE -> org.bukkit.inventory.ItemFlag.HIDE_DYE;
      case HIDE_ARMOR_TRIM -> org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM;
      case HIDE_ADDITIONAL_TOOLTIP -> org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
    };
  }
}
