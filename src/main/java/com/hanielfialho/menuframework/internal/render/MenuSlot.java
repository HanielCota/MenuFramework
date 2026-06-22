package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.api.MenuClickHandler;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/**
 * Conteúdo interno imutável de um slot renderizado.
 *
 * @param <S> tipo de estado da sessão
 */
public final class MenuSlot<S> {

  private final ItemStack icon;
  private final MenuClickHandler<S> clickHandler;

  private MenuSlot(ItemStack icon, MenuClickHandler<S> clickHandler) {
    this.icon = copyAndValidate(icon);
    this.clickHandler = clickHandler;
  }

  public static <S> MenuSlot<S> item(ItemStack icon) {
    return new MenuSlot<>(icon, null);
  }

  public static <S> MenuSlot<S> button(ItemStack icon, MenuClickHandler<S> clickHandler) {
    return new MenuSlot<>(icon, Objects.requireNonNull(clickHandler, "clickHandler"));
  }

  private static ItemStack copyAndValidate(ItemStack icon) {
    Objects.requireNonNull(icon, "icon");

    if (icon.getType().isAir()) {
      throw new IllegalArgumentException(
          "AIR cannot be used as a menu icon; leave the slot empty instead");
    }

    if (icon.getAmount() <= 0) {
      throw new IllegalArgumentException(
          "Menu icon amount must be greater than zero: " + icon.getAmount());
    }

    return icon.clone();
  }

  public ItemStack icon() {
    return this.icon.clone();
  }

  public boolean clickable() {
    return this.clickHandler != null;
  }

  public Optional<MenuClickHandler<S>> clickHandler() {
    return Optional.ofNullable(this.clickHandler);
  }

  public boolean hasSameVisual(MenuSlot<?> other) {
    return other != null && this.icon.equals(other.icon);
  }
}
