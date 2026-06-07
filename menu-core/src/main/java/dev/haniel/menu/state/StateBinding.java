package dev.haniel.menu.state;

import java.util.List;

/**
 * A first-class collection of the states bound to one view.
 *
 * <p>Binding registers the view as the listener of every state; unbinding clears them all. The view
 * unbinds on close so a closed view leaves no references behind for the garbage collector.
 */
public final class StateBinding {

  private final List<State<?>> states;

  /**
   * Wraps the states discovered on a menu instance.
   *
   * @param states the states to manage; never null, copied defensively
   */
  public StateBinding(List<State<?>> states) {
    this.states = List.copyOf(states);
  }

  /**
   * Binds every state to the given listener.
   *
   * @param listener the view to notify on change; never null
   */
  public void bind(StateListener listener) {
    states.forEach(state -> state.bind(listener));
  }

  /** Unbinds every state, removing the listener references. */
  public void unbind() {
    states.forEach(State::unbind);
  }
}
