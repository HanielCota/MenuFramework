package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.feedback.MenuFeedbackSignal;
import com.hanielfialho.menuframework.api.feedback.StandardMenuFeedbackSignals;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.inventory.ItemStack;

/**
 * Reusable button component with enabled, disabled and hidden states.
 *
 * <p>A hidden button leaves its slot unassigned, so the frame background may still fill that slot.
 * A disabled button renders an item without registering a click handler. A click handler is still
 * required at build time because the same component may become enabled in a later render.
 *
 * @param <S> menu-state type
 */
public final class MenuButton<S> implements MenuComponent<S> {

  private final SlotTarget target;
  private final MenuItemProvider<S> enabledIcon;
  private final MenuItemProvider<S> disabledIcon;
  private final Predicate<MenuRenderContext<S>> visibility;
  private final Predicate<MenuRenderContext<S>> enabled;
  private final MenuClickHandler<S> clickHandler;
  private final MenuFeedbackSignal feedbackSignal;

  private MenuButton(Builder<S> builder) {
    this.target = builder.target;
    this.enabledIcon =
        Objects.requireNonNull(builder.enabledIcon, "An enabled icon must be configured");
    this.disabledIcon = builder.disabledIcon == null ? this.enabledIcon : builder.disabledIcon;
    this.visibility = builder.visibility;
    this.enabled = builder.enabled;
    this.clickHandler =
        Objects.requireNonNull(builder.clickHandler, "A click handler must be configured");
    this.feedbackSignal = builder.feedbackSignal;
  }

  /**
   * Creates a builder bound to a raw slot.
   *
   * @param slot raw slot
   * @param <S> menu-state type
   * @return builder
   */
  public static <S> Builder<S> at(int slot) {
    return new Builder<>(layout -> layout.checkSlot(slot));
  }

  /**
   * Creates a builder bound to a named slot.
   *
   * @param namedSlot required named slot
   * @param <S> menu-state type
   * @return builder
   */
  public static <S> Builder<S> at(String namedSlot) {
    String checkedName = Objects.requireNonNull(namedSlot, "namedSlot");
    return new Builder<>(layout -> layout.slot(checkedName));
  }

  /**
   * Resolves this button's visual state.
   *
   * @param context render snapshot
   * @return resolved state
   */
  public MenuButtonState state(MenuRenderContext<S> context) {
    Objects.requireNonNull(context, "context");

    if (!this.visibility.test(context)) {
      return MenuButtonState.HIDDEN;
    }

    return this.enabled.test(context) ? MenuButtonState.ENABLED : MenuButtonState.DISABLED;
  }

  /** {@inheritDoc} */
  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(canvas, "canvas");

    int slot = this.target.resolve(canvas.layout());
    MenuButtonState state = this.state(context);

    switch (state) {
      case HIDDEN -> {
        return;
      }
      case DISABLED -> canvas.item(slot, this.disabledIcon.provide(context));
      case ENABLED ->
          canvas.button(
              slot,
              this.enabledIcon.provide(context),
              interaction -> {
                if (this.feedbackSignal != null) {
                  interaction.feedback(this.feedbackSignal);
                }
                this.clickHandler.handle(interaction);
              });
    }
  }

  @FunctionalInterface
  private interface SlotTarget {
    int resolve(MenuLayout layout);
  }

  /** Mutable, non-thread-safe builder for {@link MenuButton}. */
  public static final class Builder<S> {

    private final SlotTarget target;
    private MenuItemProvider<S> enabledIcon;
    private MenuItemProvider<S> disabledIcon;
    private Predicate<MenuRenderContext<S>> visibility = context -> true;
    private Predicate<MenuRenderContext<S>> enabled = context -> true;
    private MenuClickHandler<S> clickHandler;
    private MenuFeedbackSignal feedbackSignal = StandardMenuFeedbackSignals.BUTTON_CLICK;

    private Builder(SlotTarget target) {
      this.target = Objects.requireNonNull(target, "target");
    }

    /**
     * Sets a fixed enabled icon.
     *
     * @param icon icon template
     * @return this builder
     */
    public Builder<S> icon(ItemStack icon) {
      return this.icon(MenuItemProvider.fixed(icon));
    }

    /**
     * Sets a dynamic enabled icon.
     *
     * @param provider icon provider
     * @return this builder
     */
    public Builder<S> icon(MenuItemProvider<S> provider) {
      this.enabledIcon = Objects.requireNonNull(provider, "provider");
      return this;
    }

    /**
     * Sets a fixed disabled icon.
     *
     * <p>When omitted, the enabled icon is rendered without a handler.
     *
     * @param icon disabled icon template
     * @return this builder
     */
    public Builder<S> disabledIcon(ItemStack icon) {
      return this.disabledIcon(MenuItemProvider.fixed(icon));
    }

    /**
     * Sets a dynamic disabled icon.
     *
     * @param provider disabled icon provider
     * @return this builder
     */
    public Builder<S> disabledIcon(MenuItemProvider<S> provider) {
      this.disabledIcon = Objects.requireNonNull(provider, "provider");
      return this;
    }

    /**
     * Adds a visibility condition. Conditions are combined with logical AND.
     *
     * <p>When a button is not visible, this component does not assign its target slot.
     *
     * @param condition visibility predicate
     * @return this builder
     */
    public Builder<S> visibleWhen(Predicate<MenuRenderContext<S>> condition) {
      this.visibility = this.visibility.and(Objects.requireNonNull(condition, "condition"));
      return this;
    }

    /**
     * Adds a hidden condition. Conditions are combined with logical OR across calls to this method
     * and logical AND with explicit {@link #visibleWhen(Predicate)} conditions.
     *
     * @param condition hidden predicate
     * @return this builder
     */
    public Builder<S> hiddenWhen(Predicate<MenuRenderContext<S>> condition) {
      Objects.requireNonNull(condition, "condition");
      return this.visibleWhen(condition.negate());
    }

    /**
     * Adds an enabled condition. Conditions are combined with logical AND.
     *
     * <p>When a button is disabled, the disabled icon is rendered without a handler. If no disabled
     * icon was configured, the enabled icon is reused as a static item.
     *
     * @param condition enabled predicate
     * @return this builder
     */
    public Builder<S> enabledWhen(Predicate<MenuRenderContext<S>> condition) {
      this.enabled = this.enabled.and(Objects.requireNonNull(condition, "condition"));
      return this;
    }

    /**
     * Adds a disabled condition.
     *
     * @param condition disabled predicate
     * @return this builder
     */
    public Builder<S> disabledWhen(Predicate<MenuRenderContext<S>> condition) {
      Objects.requireNonNull(condition, "condition");
      return this.enabledWhen(condition.negate());
    }

    /**
     * Sets the click handler.
     *
     * @param clickHandler handler
     * @return this builder
     */
    public Builder<S> onClick(MenuClickHandler<S> clickHandler) {
      this.clickHandler = Objects.requireNonNull(clickHandler, "clickHandler");
      return this;
    }

    /**
     * Sets the feedback signal buffered before the click handler is invoked.
     *
     * @param feedbackSignal signal
     * @return this builder
     */
    public Builder<S> feedback(MenuFeedbackSignal feedbackSignal) {
      this.feedbackSignal = Objects.requireNonNull(feedbackSignal, "feedbackSignal");
      return this;
    }

    /**
     * Disables automatic feedback for this button.
     *
     * @return this builder
     */
    public Builder<S> withoutFeedback() {
      this.feedbackSignal = null;
      return this;
    }

    /**
     * Builds the immutable component.
     *
     * @return menu button
     */
    public MenuButton<S> build() {
      return new MenuButton<>(this);
    }
  }
}
