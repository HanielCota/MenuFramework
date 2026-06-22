package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.feedback.StandardMenuFeedbackSignals;
import com.hanielfialho.menuframework.api.theme.MenuThemeKey;
import com.hanielfialho.menuframework.api.theme.StandardMenuThemeKeys;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Factory methods for common reusable menu components. */
public final class MenuComponents {

  private MenuComponents() {}

  /**
   * Creates an ordered composition.
   *
   * @param components components
   * @param <S> menu-state type
   * @return composition
   */
  @SafeVarargs
  public static <S> CompositeMenuComponent<S> compose(MenuComponent<S>... components) {
    Objects.requireNonNull(components, "components");

    List<MenuComponent<S>> copy = new ArrayList<>(components.length);
    for (MenuComponent<S> component : components) {
      copy.add(Objects.requireNonNull(component, "components contains null"));
    }

    return CompositeMenuComponent.copyOf(copy);
  }

  /**
   * Creates an ordered composition.
   *
   * @param components components
   * @param <S> menu-state type
   * @return composition
   */
  public static <S> CompositeMenuComponent<S> compose(
      Collection<? extends MenuComponent<S>> components) {
    return CompositeMenuComponent.copyOf(components);
  }

  /**
   * Creates the standard themed background.
   *
   * @param <S> menu-state type
   * @return background component
   */
  public static <S> BackgroundComponent<S> background() {
    return BackgroundComponent.themed(StandardMenuThemeKeys.BACKGROUND);
  }

  /**
   * Creates a themed item component at a raw slot.
   *
   * @param slot raw slot
   * @param key theme key
   * @param <S> menu-state type
   * @return item component
   */
  public static <S> MenuComponent<S> themedItem(int slot, MenuThemeKey key) {
    MenuItemProvider<S> provider = MenuItemProvider.themed(key);
    return (context, canvas) -> canvas.item(slot, provider.provide(context));
  }

  /**
   * Creates a themed item component at a named slot.
   *
   * @param namedSlot named slot
   * @param key theme key
   * @param <S> menu-state type
   * @return item component
   */
  public static <S> MenuComponent<S> themedItem(String namedSlot, MenuThemeKey key) {
    String checkedSlot = Objects.requireNonNull(namedSlot, "namedSlot");
    MenuItemProvider<S> provider = MenuItemProvider.themed(key);
    return (context, canvas) -> canvas.item(checkedSlot, provider.provide(context));
  }

  /**
   * Fills every slot in a named region with one dynamically supplied icon.
   *
   * @param namedRegion named region
   * @param provider icon provider evaluated once per render
   * @param <S> menu-state type
   * @return region-fill component
   */
  public static <S> MenuComponent<S> fillRegion(String namedRegion, MenuItemProvider<S> provider) {
    String checkedRegion = Objects.requireNonNull(namedRegion, "namedRegion");
    MenuItemProvider<S> checkedProvider = Objects.requireNonNull(provider, "provider");
    return (context, canvas) -> canvas.region(checkedRegion).fill(checkedProvider.provide(context));
  }

  /**
   * Fills every slot in a named region with a themed icon.
   *
   * @param namedRegion named region
   * @param key theme key
   * @param <S> menu-state type
   * @return region-fill component
   */
  public static <S> MenuComponent<S> fillRegion(String namedRegion, MenuThemeKey key) {
    return fillRegion(namedRegion, MenuItemProvider.themed(key));
  }

  /**
   * Marks every slot in a named region as explicitly empty.
   *
   * @param namedRegion named region
   * @param <S> menu-state type
   * @return region-emptying component
   */
  public static <S> MenuComponent<S> emptyRegion(String namedRegion) {
    String checkedRegion = Objects.requireNonNull(namedRegion, "namedRegion");
    return (context, canvas) -> canvas.region(checkedRegion).empty();
  }

  /**
   * Renders a component only when a predicate evaluates to {@code true}.
   *
   * @param condition render condition
   * @param component delegated component
   * @param <S> menu-state type
   * @return conditional component
   */
  public static <S> MenuComponent<S> when(
      Predicate<MenuRenderContext<S>> condition, MenuComponent<S> component) {
    Predicate<MenuRenderContext<S>> checkedCondition =
        Objects.requireNonNull(condition, "condition");
    MenuComponent<S> checkedComponent = Objects.requireNonNull(component, "component");
    return (context, canvas) -> {
      if (checkedCondition.test(context)) {
        checkedComponent.render(context, canvas);
      }
    };
  }

  /**
   * Creates the standard loading indicator at a named slot.
   *
   * @param namedSlot named slot
   * @param <S> menu-state type
   * @return loading component
   */
  public static <S> MenuComponent<S> loadingIndicator(String namedSlot) {
    return themedItem(namedSlot, StandardMenuThemeKeys.LOADING);
  }

  /**
   * Creates a standard close button at a raw slot.
   *
   * @param slot raw slot
   * @param <S> menu-state type
   * @return close button
   */
  public static <S> MenuButton<S> closeButton(int slot) {
    return closeButton(MenuButton.<S>at(slot));
  }

  /**
   * Creates a standard close button at a named slot.
   *
   * @param namedSlot named slot
   * @param <S> menu-state type
   * @return close button
   */
  public static <S> MenuButton<S> closeButton(String namedSlot) {
    return closeButton(MenuButton.<S>at(namedSlot));
  }

  /**
   * Creates a standard back button at a raw slot.
   *
   * @param slot raw slot
   * @param <S> menu-state type
   * @return back button
   */
  public static <S> MenuButton<S> backButton(int slot) {
    return backButton(MenuButton.<S>at(slot));
  }

  /**
   * Creates a standard back button at a named slot.
   *
   * @param namedSlot named slot
   * @param <S> menu-state type
   * @return back button
   */
  public static <S> MenuButton<S> backButton(String namedSlot) {
    return backButton(MenuButton.<S>at(namedSlot));
  }

  /**
   * Creates a themed previous-page button.
   *
   * @param namedSlot named slot
   * @param enabled enabled predicate
   * @param handler page transition handler
   * @param <S> menu-state type
   * @return previous-page button
   */
  public static <S> MenuButton<S> previousPageButton(
      String namedSlot, Predicate<MenuRenderContext<S>> enabled, MenuClickHandler<S> handler) {
    return MenuButton.<S>at(namedSlot)
        .icon(MenuItemProvider.themed(StandardMenuThemeKeys.PREVIOUS_ENABLED))
        .disabledIcon(MenuItemProvider.themed(StandardMenuThemeKeys.PREVIOUS_DISABLED))
        .enabledWhen(enabled)
        .feedback(StandardMenuFeedbackSignals.PAGE_PREVIOUS)
        .onClick(handler)
        .build();
  }

  /**
   * Creates a themed next-page button.
   *
   * @param namedSlot named slot
   * @param enabled enabled predicate
   * @param handler page transition handler
   * @param <S> menu-state type
   * @return next-page button
   */
  public static <S> MenuButton<S> nextPageButton(
      String namedSlot, Predicate<MenuRenderContext<S>> enabled, MenuClickHandler<S> handler) {
    return MenuButton.<S>at(namedSlot)
        .icon(MenuItemProvider.themed(StandardMenuThemeKeys.NEXT_ENABLED))
        .disabledIcon(MenuItemProvider.themed(StandardMenuThemeKeys.NEXT_DISABLED))
        .enabledWhen(enabled)
        .feedback(StandardMenuFeedbackSignals.PAGE_NEXT)
        .onClick(handler)
        .build();
  }

  /**
   * Creates a standard retry button.
   *
   * @param namedSlot named slot
   * @param handler retry handler
   * @param <S> menu-state type
   * @return retry button
   */
  public static <S> MenuButton<S> retryButton(String namedSlot, MenuClickHandler<S> handler) {
    return MenuButton.<S>at(namedSlot)
        .icon(MenuItemProvider.themed(StandardMenuThemeKeys.ERROR))
        .feedback(StandardMenuFeedbackSignals.RETRY)
        .onClick(handler)
        .build();
  }

  private static <S> MenuButton<S> closeButton(MenuButton.Builder<S> builder) {
    return builder
        .icon(MenuItemProvider.themed(StandardMenuThemeKeys.CLOSE))
        .feedback(StandardMenuFeedbackSignals.MENU_CLOSE)
        .onClick(MenuInteraction::close)
        .build();
  }

  private static <S> MenuButton<S> backButton(MenuButton.Builder<S> builder) {
    return builder
        .icon(MenuItemProvider.themed(StandardMenuThemeKeys.BACK_ENABLED))
        .disabledIcon(MenuItemProvider.themed(StandardMenuThemeKeys.BACK_DISABLED))
        .enabledWhen(MenuRenderContext::canGoBack)
        .feedback(StandardMenuFeedbackSignals.NAVIGATION_BACK)
        .onClick(MenuInteraction::back)
        .build();
  }
}
