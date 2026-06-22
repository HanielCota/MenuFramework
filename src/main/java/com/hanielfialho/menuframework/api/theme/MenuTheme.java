package com.hanielfialho.menuframework.api.theme;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import org.bukkit.inventory.ItemStack;

/**
 * Resolves visual assets used by reusable menu components.
 *
 * <p>Implementations may return a new item or a shared template. The menu canvas always performs a
 * defensive copy before publishing a frame.
 */
@FunctionalInterface
public interface MenuTheme {

  /**
   * Resolves a required icon.
   *
   * @param key icon key
   * @param context current render snapshot
   * @return non-null, non-air icon
   * @throws IllegalArgumentException when the key is unsupported
   */
  ItemStack item(MenuThemeKey key, MenuRenderContext<?> context);

  /**
   * Returns the built-in immutable theme.
   *
   * @return default theme
   */
  static MenuTheme defaults() {
    return DefaultMenuTheme.instance();
  }
}
