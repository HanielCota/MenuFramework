package dev.haniel.menu.paper.reactive;

import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.state.StateListener;
import java.util.Objects;

/**
 * Ties a view's bound states to its coalescing flusher.
 *
 * <p>Binding registers the view as the listener of every state; closing unbinds them and cancels
 * any pending flush — the anti-leak guarantee for reactive views.
 */
public final class ReactiveBinding {

  private final StateBinding states;
  private final Flusher flusher;

  /**
   * Pairs the bound states with the flusher.
   *
   * @param states the states discovered on the instance; never null
   * @param flusher the coalescing flusher; never null
   */
  public ReactiveBinding(StateBinding states, Flusher flusher) {
    this.states = Objects.requireNonNull(states, "states");
    this.flusher = Objects.requireNonNull(flusher, "flusher");
  }

  /**
   * Binds every state to the given listener.
   *
   * @param listener the view; never null
   */
  public void bind(StateListener listener) {
    states.bind(listener);
  }

  /** Schedules a coalesced re-render. */
  public void schedule() {
    flusher.mark();
  }

  /** Unbinds all state and cancels any pending flush. */
  public void close() {
    states.unbind();
    flusher.cancel();
  }
}
