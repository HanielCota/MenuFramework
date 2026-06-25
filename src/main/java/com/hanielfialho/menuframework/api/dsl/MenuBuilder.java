package com.hanielfialho.menuframework.api.dsl;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Declarative menu builder for the most common cases.
 *
 * <p>Produces a reusable {@link Menu} instance backed by the builder configuration. The returned
 * menu is stateless and safe to share between viewers.
 *
 * @param <S> session-state type
 */
public final class MenuBuilder<S> {

  private final int rows;
  private final Component title;
  private MenuItemFactory<S> background;
  private final List<MenuComponent<S>> components = new ArrayList<>();

  private MenuBuilder(int rows, Component title) {
    this.rows = rows;
    this.title = Objects.requireNonNull(title, "title");
  }

  /**
   * Starts a builder for a chest menu with the supplied title.
   *
   * @param rows number of rows, from 1 to 6
   * @param title menu title
   * @param <S> state type
   * @return builder
   */
  public static <S> MenuBuilder<S> chest(int rows, Component title) {
    return new MenuBuilder<>(rows, title);
  }

  /**
   * Starts a builder for a chest menu with a plain-text title.
   *
   * @param rows number of rows, from 1 to 6
   * @param title plain-text title
   * @param <S> state type
   * @return builder
   */
  public static <S> MenuBuilder<S> chest(int rows, String title) {
    return chest(rows, Component.text(title));
  }

  /**
   * Defines the background icon for unassigned slots.
   *
   * @param icon background icon
   * @return this builder
   */
  public MenuBuilder<S> background(ItemStack icon) {
    return this.background(MenuItemFactory.fixed(icon));
  }

  /**
   * Defines a dynamic background icon.
   *
   * @param factory background factory
   * @return this builder
   */
  public MenuBuilder<S> background(MenuItemFactory<S> factory) {
    this.background = Objects.requireNonNull(factory, "factory");
    return this;
  }

  /**
   * Places a static item at a named slot.
   *
   * @param namedSlot named slot declared by the layout
   * @param icon static icon
   * @return this builder
   */
  public MenuBuilder<S> item(String namedSlot, ItemStack icon) {
    return this.item(namedSlot, MenuItemFactory.fixed(icon));
  }

  /**
   * Places a dynamic item at a named slot.
   *
   * @param namedSlot named slot declared by the layout
   * @param factory icon factory
   * @return this builder
   */
  public MenuBuilder<S> item(String namedSlot, MenuItemFactory<S> factory) {
    String checkedSlot = Objects.requireNonNull(namedSlot, "namedSlot");
    MenuItemFactory<S> checkedFactory = Objects.requireNonNull(factory, "factory");
    this.components.add(
        (context, canvas) -> canvas.item(checkedSlot, checkedFactory.create(context)));
    return this;
  }

  /**
   * Places a static item at coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param icon static icon
   * @return this builder
   */
  public MenuBuilder<S> item(int row, int column, ItemStack icon) {
    return this.item(row, column, MenuItemFactory.fixed(icon));
  }

  /**
   * Places a dynamic item at coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param factory icon factory
   * @return this builder
   */
  public MenuBuilder<S> item(int row, int column, MenuItemFactory<S> factory) {
    MenuItemFactory<S> checkedFactory = Objects.requireNonNull(factory, "factory");
    this.components.add(
        (context, canvas) -> canvas.item(row, column, checkedFactory.create(context)));
    return this;
  }

  /**
   * Places a button at a named slot.
   *
   * @param namedSlot named slot
   * @param icon static icon
   * @param handler click handler
   * @return this builder
   */
  public MenuBuilder<S> button(String namedSlot, ItemStack icon, MenuClickHandler<S> handler) {
    return this.button(namedSlot, MenuItemFactory.fixed(icon), handler);
  }

  /**
   * Places a dynamic button at a named slot.
   *
   * @param namedSlot named slot
   * @param factory icon factory
   * @param handler click handler
   * @return this builder
   */
  public MenuBuilder<S> button(
      String namedSlot, MenuItemFactory<S> factory, MenuClickHandler<S> handler) {
    String checkedSlot = Objects.requireNonNull(namedSlot, "namedSlot");
    MenuItemFactory<S> checkedFactory = Objects.requireNonNull(factory, "factory");
    MenuClickHandler<S> checkedHandler = Objects.requireNonNull(handler, "handler");
    this.components.add(
        (context, canvas) ->
            canvas.button(checkedSlot, checkedFactory.create(context), checkedHandler));
    return this;
  }

  /**
   * Places a button at coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param icon static icon
   * @param handler click handler
   * @return this builder
   */
  public MenuBuilder<S> button(int row, int column, ItemStack icon, MenuClickHandler<S> handler) {
    return this.button(row, column, MenuItemFactory.fixed(icon), handler);
  }

  /**
   * Places a dynamic button at coordinates.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @param factory icon factory
   * @param handler click handler
   * @return this builder
   */
  public MenuBuilder<S> button(
      int row, int column, MenuItemFactory<S> factory, MenuClickHandler<S> handler) {
    MenuItemFactory<S> checkedFactory = Objects.requireNonNull(factory, "factory");
    MenuClickHandler<S> checkedHandler = Objects.requireNonNull(handler, "handler");
    this.components.add(
        (context, canvas) ->
            canvas.button(row, column, checkedFactory.create(context), checkedHandler));
    return this;
  }

  /**
   * Renders a reusable component.
   *
   * @param component component to render
   * @return this builder
   */
  public MenuBuilder<S> component(MenuComponent<S> component) {
    this.components.add(Objects.requireNonNull(component, "component"));
    return this;
  }

  /**
   * Renders a component only when a predicate is satisfied.
   *
   * @param condition render condition
   * @param component component to render
   * @return this builder
   */
  public MenuBuilder<S> when(
      java.util.function.Predicate<MenuRenderContext<S>> condition, MenuComponent<S> component) {
    java.util.function.Predicate<MenuRenderContext<S>> checkedCondition =
        Objects.requireNonNull(condition, "condition");
    MenuComponent<S> checkedComponent = Objects.requireNonNull(component, "component");
    this.components.add(
        (context, canvas) -> {
          if (checkedCondition.test(context)) {
            checkedComponent.render(context, canvas);
          }
        });
    return this;
  }

  /**
   * Adds a close button at a named slot using the configured theme.
   *
   * @param namedSlot named slot
   * @return this builder
   */
  public MenuBuilder<S> closeButton(String namedSlot) {
    this.components.add(
        com.hanielfialho.menuframework.api.component.MenuComponents.closeButton(namedSlot));
    return this;
  }

  /**
   * Adds a back button at a named slot using the configured theme.
   *
   * @param namedSlot named slot
   * @return this builder
   */
  public MenuBuilder<S> backButton(String namedSlot) {
    this.components.add(
        com.hanielfialho.menuframework.api.component.MenuComponents.backButton(namedSlot));
    return this;
  }

  /**
   * Adds a toggle button at a named slot.
   *
   * @param namedSlot named slot
   * @param label button label
   * @param reader state reader
   * @param writer state writer
   * @return this builder
   */
  public MenuBuilder<S> toggle(
      String namedSlot,
      String label,
      java.util.function.Function<? super S, Boolean> reader,
      java.util.function.BiFunction<? super S, Boolean, ? extends S> writer) {
    this.components.add(
        com.hanielfialho.menuframework.api.component.ToggleButton.<S>at(namedSlot)
            .label(label)
            .reader(reader)
            .writer(writer)
            .build());
    return this;
  }

  /**
   * Builds the reusable menu definition.
   *
   * @return menu definition
   */
  public Menu<S> build() {
    return new SimpleMenu<>(this.rows, this.title, this.background, List.copyOf(this.components));
  }

  private record SimpleMenu<S>(
      int rows, Component title, MenuItemFactory<S> background, List<MenuComponent<S>> components)
      implements Menu<S> {

    @Override
    public MenuLayout layout() {
      return MenuLayout.chest(this.rows);
    }

    @Override
    public Component title(MenuRenderContext<S> context) {
      return this.title;
    }

    @Override
    public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
      if (this.background != null) {
        canvas.background(this.background.create(context));
      }

      for (MenuComponent<S> component : this.components) {
        component.render(context, canvas);
      }
    }
  }
}
