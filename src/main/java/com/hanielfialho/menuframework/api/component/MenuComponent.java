package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Objects;

/**
 * Reusable rendering unit that contributes zero or more slots to a complete frame.
 *
 * @param <S> menu-state type
 */
@FunctionalInterface
public interface MenuComponent<S> {

  /**
   * Renders this component.
   *
   * @param context current render snapshot
   * @param canvas current single-use canvas
   */
  void render(MenuRenderContext<S> context, MenuCanvas<S> canvas);

  /**
   * Composes this component with another component.
   *
   * @param next component rendered afterwards
   * @return composed component
   */
  default MenuComponent<S> andThen(MenuComponent<S> next) {
    Objects.requireNonNull(next, "next");
    return (context, canvas) -> {
      this.render(context, canvas);
      next.render(context, canvas);
    };
  }
}
