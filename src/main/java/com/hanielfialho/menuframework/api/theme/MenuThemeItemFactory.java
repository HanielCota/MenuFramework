package com.hanielfialho.menuframework.api.theme;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import org.bukkit.inventory.ItemStack;

/** Produces a themed icon for a concrete render snapshot. */
@FunctionalInterface
public interface MenuThemeItemFactory {

  /**
   * Creates an icon.
   *
   * @param context current render snapshot
   * @return non-null, non-air icon
   */
  ItemStack create(MenuRenderContext<?> context);
}
