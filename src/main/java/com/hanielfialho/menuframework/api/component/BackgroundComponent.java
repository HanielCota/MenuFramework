package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.theme.MenuThemeKey;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Reusable frame-background component. */
public final class BackgroundComponent<S> implements MenuComponent<S> {

  private final MenuItemProvider<S> provider;

  private BackgroundComponent(MenuItemProvider<S> provider) {
    this.provider = Objects.requireNonNull(provider, "provider");
  }

  /**
   * Creates a fixed background component.
   *
   * @param item background icon
   * @param <S> menu-state type
   * @return component
   */
  public static <S> BackgroundComponent<S> fixed(ItemStack item) {
    return new BackgroundComponent<>(MenuItemProvider.fixed(item));
  }

  /**
   * Creates a themed background component.
   *
   * @param key theme key
   * @param <S> menu-state type
   * @return component
   */
  public static <S> BackgroundComponent<S> themed(MenuThemeKey key) {
    return new BackgroundComponent<>(MenuItemProvider.themed(key));
  }

  /**
   * Creates a dynamic background component.
   *
   * @param provider icon provider
   * @param <S> menu-state type
   * @return component
   */
  public static <S> BackgroundComponent<S> provided(MenuItemProvider<S> provider) {
    return new BackgroundComponent<>(provider);
  }

  /** {@inheritDoc} */
  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    canvas.background(this.provider.provide(context));
  }
}
