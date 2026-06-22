package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Immutable ordered composition of reusable menu components. */
public final class CompositeMenuComponent<S> implements MenuComponent<S> {

  private final List<MenuComponent<S>> components;

  private CompositeMenuComponent(Collection<? extends MenuComponent<S>> components) {
    Objects.requireNonNull(components, "components");
    this.components =
        components.stream()
            .map(component -> Objects.requireNonNull(component, "components contains null"))
            .toList();
  }

  /**
   * Creates an ordered composition.
   *
   * @param components components rendered in declaration order
   * @param <S> menu-state type
   * @return immutable composition
   */
  @SafeVarargs
  public static <S> CompositeMenuComponent<S> of(MenuComponent<S>... components) {
    Objects.requireNonNull(components, "components");

    List<MenuComponent<S>> copy = new ArrayList<>(components.length);
    for (MenuComponent<S> component : components) {
      copy.add(Objects.requireNonNull(component, "components contains null"));
    }

    return new CompositeMenuComponent<>(copy);
  }

  /**
   * Creates an ordered composition from a collection.
   *
   * @param components components rendered in iteration order
   * @param <S> menu-state type
   * @return immutable composition
   */
  public static <S> CompositeMenuComponent<S> copyOf(
      Collection<? extends MenuComponent<S>> components) {
    return new CompositeMenuComponent<>(components);
  }

  /**
   * Returns the immutable component list.
   *
   * @return components in render order
   */
  public List<MenuComponent<S>> components() {
    return this.components;
  }

  /** {@inheritDoc} */
  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    for (MenuComponent<S> component : this.components) {
      component.render(context, canvas);
    }
  }
}
