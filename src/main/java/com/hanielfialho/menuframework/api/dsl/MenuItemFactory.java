package com.hanielfialho.menuframework.api.dsl;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;

/**
 * Factory used by the declarative DSL to resolve icons from static or dynamic sources.
 *
 * @param <S> menu-state type
 */
@FunctionalInterface
public interface MenuItemFactory<S> {

  /**
   * Resolves an icon for the current render.
   *
   * @param context current render snapshot
   * @return non-null, non-air icon
   */
  ItemStack create(MenuRenderContext<S> context);

  /**
   * Creates a factory that returns the same icon every time.
   *
   * @param icon static icon
   * @param <S> menu-state type
   * @return fixed factory
   */
  static <S> MenuItemFactory<S> fixed(ItemStack icon) {
    ItemStack checked = Objects.requireNonNull(icon, "icon");
    return context -> checked;
  }

  /**
   * Creates a factory from a state-derived function.
   *
   * @param resolver state resolver
   * @param <S> menu-state type
   * @return dynamic factory
   */
  static <S> MenuItemFactory<S> of(Function<? super S, ItemStack> resolver) {
    Objects.requireNonNull(resolver, "resolver");
    return context -> resolver.apply(context.state());
  }

  /**
   * Creates a factory from a render-context function.
   *
   * @param resolver context resolver
   * @param <S> menu-state type
   * @return dynamic factory
   */
  static <S> MenuItemFactory<S> ofContext(Function<MenuRenderContext<S>, ItemStack> resolver) {
    return Objects.requireNonNull(resolver, "resolver")::apply;
  }
}
