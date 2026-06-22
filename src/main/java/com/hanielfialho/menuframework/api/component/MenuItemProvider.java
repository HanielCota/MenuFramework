package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.theme.MenuThemeKey;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Produces an icon from a menu render snapshot. */
@FunctionalInterface
public interface MenuItemProvider<S> {

  /**
   * Produces an icon.
   *
   * @param context current render snapshot
   * @return non-null, non-air icon
   */
  ItemStack provide(MenuRenderContext<S> context);

  /**
   * Creates a provider backed by a defensive item template.
   *
   * @param item icon template
   * @param <S> menu-state type
   * @return fixed provider
   */
  static <S> MenuItemProvider<S> fixed(ItemStack item) {
    ItemStack snapshot = copyAndValidate(item);
    return context -> snapshot.clone();
  }

  /**
   * Creates a provider that resolves one key from the render context's theme.
   *
   * @param key theme key
   * @param <S> menu-state type
   * @return themed provider
   */
  static <S> MenuItemProvider<S> themed(MenuThemeKey key) {
    MenuThemeKey checkedKey = Objects.requireNonNull(key, "key");
    return context -> context.theme().item(checkedKey, context);
  }

  private static ItemStack copyAndValidate(ItemStack item) {
    ItemStack checked = Objects.requireNonNull(item, "item");

    if (checked.getType().isAir() || checked.getAmount() <= 0) {
      throw new IllegalArgumentException("A menu item provider requires a non-air icon");
    }

    return checked.clone();
  }
}
