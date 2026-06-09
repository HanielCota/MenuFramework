package dev.haniel.menu.paper.reactive;

import dev.haniel.menu.state.StateListener;
import java.util.Objects;

/**
 * The teardown-bearing lifecycle of one open reactive view: its state binding plus its ticks.
 *
 * <p>Groups the two things created at open and discarded at close, so a view keeps a single
 * lifecycle field. Binding starts ticking; closing both unbinds state and cancels every tick.
 */
public final class ReactiveLifecycle {

  private final ReactiveBinding reactive;
  private final Ticking ticking;
  private final Runnable onClose;
  private boolean closed;

  /**
   * Pairs the reactive state binding with the view's ticks and its close hook.
   *
   * @param reactive the coalescing state binding; never null
   * @param ticking the view's periodic ticks; never null
   * @param onClose the action to run when the view closes (after teardown); never null
   */
  public ReactiveLifecycle(ReactiveBinding reactive, Ticking ticking, Runnable onClose) {
    this.reactive = Objects.requireNonNull(reactive, "reactive");
    this.ticking = Objects.requireNonNull(ticking, "ticking");
    this.onClose = Objects.requireNonNull(onClose, "onClose");
  }

  /**
   * Binds the view to its states and starts its ticks.
   *
   * @param listener the view; never null
   */
  public void bind(StateListener listener) {
    reactive.bind(listener);
    ticking.start();
  }

  /** Schedules a coalesced re-render. */
  public void schedule() {
    reactive.schedule();
  }

  /**
   * Unbinds all state, cancels any pending flush, stops every tick and runs the close hook.
   *
   * <p>Idempotent: a second call is a no-op, so the close hook (and any user {@code @OnClose}) runs
   * exactly once even when both a failed open and a later close event reach this lifecycle.
   */
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    reactive.close();
    ticking.cancel();
    onClose.run();
  }
}
