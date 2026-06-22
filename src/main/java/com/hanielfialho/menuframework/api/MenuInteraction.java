package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.api.task.MenuAsyncActions;
import com.hanielfialho.menuframework.api.task.MenuTaskActions;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Deferred command buffer available to a click handler.
 *
 * <p>The interaction is valid only while its handler is running. Commands are applied after a
 * successful return; when the handler throws, the entire interaction buffer is discarded.
 *
 * <p>Terminal commands ({@link #close()}, {@link #open(Menu, Object)} and {@link #back()}) cannot
 * be combined with state changes, periodic-task commands or an asynchronous operation in the same
 * interaction.
 *
 * @param <S> session-state type
 */
public interface MenuInteraction<S>
    extends MenuAsyncActions<S>, MenuTaskActions<S>, MenuNavigationContext {

  /**
   * Returns the immutable click snapshot that invoked the handler.
   *
   * @return the current click
   */
  MenuClick click();

  /**
   * Replaces the session state and requests a complete render.
   *
   * @param newState non-null replacement state
   * @throws NullPointerException if {@code newState} is {@code null}
   * @throws IllegalStateException if a terminal command was already recorded
   */
  void setState(S newState);

  /**
   * Computes and publishes a replacement state, then requests a render.
   *
   * @param updater non-null function that must return a non-null state
   * @throws NullPointerException if the updater or its result is {@code null}
   * @throws IllegalStateException if a terminal command was already recorded
   */
  default void updateState(UnaryOperator<S> updater) {
    Objects.requireNonNull(updater, "updater");

    S updatedState =
        Objects.requireNonNull(updater.apply(this.state()), "The state updater returned null");

    this.setState(updatedState);
  }

  /**
   * Requests a complete render without replacing the state.
   *
   * @throws IllegalStateException if a terminal command was already recorded
   */
  void refresh();

  /**
   * Requests safe termination of the current session.
   *
   * @throws IllegalStateException if another terminal command or a non-terminal command was already
   *     recorded
   */
  void close();

  /**
   * Opens another menu and pushes the current menu and state onto history.
   *
   * @param menu reusable target menu
   * @param initialState non-null initial target state
   * @param <T> target state type
   * @throws NullPointerException if an argument is {@code null}
   * @throws IllegalStateException if another command was already recorded
   */
  <T> void open(Menu<T> menu, T initialState);

  /**
   * Opens a target menu that uses {@link EmptyMenuState}.
   *
   * @param menu target menu
   */
  default void open(Menu<EmptyMenuState> menu) {
    this.open(menu, EmptyMenuState.INSTANCE);
  }

  /**
   * Restores the previous menu with the state captured when it was left.
   *
   * @throws IllegalStateException if no history entry exists or another command was already
   *     recorded
   */
  void back();

  /**
   * Restores the previous menu, or closes the current root session when no history entry exists.
   */
  default void backOrClose() {
    if (!this.canGoBack()) {
      this.close();
      return;
    }
    this.back();
  }
}
