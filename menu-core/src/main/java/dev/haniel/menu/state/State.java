package dev.haniel.menu.state;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An explicit, observable holder of a value.
 *
 * <p>The reactive primitive: {@code State.of(initial)} declares it, {@code set(next)} changes it. A
 * change notifies the bound listener (the view), which schedules a re-render. There is no proxy or
 * runtime reflection — the write is detectable because it goes through {@link #set(Object)}. Before
 * a listener is bound, {@code set} just stores the value.
 *
 * <p><strong>Threading:</strong> not thread-safe by design. A state belongs to one open view and
 * must be read and written on that view's owning thread — the server main thread on Paper, the
 * player's region thread on Folia. Mutating it from an asynchronous task is unsupported: the write
 * may not be visible and {@link #set(Object)} would notify the listener off-thread. Do background
 * work off-thread, then hop back to the owning thread before calling {@code set}.
 *
 * @param <T> the held value type
 */
public final class State<T> {

  private T value;
  private @Nullable StateListener listener;

  private State(T value) {
    this.value = value;
  }

  /**
   * Creates a state holding the given initial value.
   *
   * @param initial the initial value; never null
   * @param <T> the value type
   * @return a new, unbound state
   */
  public static <T> State<T> of(T initial) {
    return new State<>(Objects.requireNonNull(initial, "initial"));
  }

  /**
   * Returns the current value.
   *
   * @return the value; never null
   */
  public T get() {
    return value;
  }

  /**
   * Sets a new value, notifying the bound listener only if it actually changed.
   *
   * @param next the new value; never null
   */
  public void set(T next) {
    Objects.requireNonNull(next, "next");
    if (Objects.equals(value, next)) {
      return;
    }
    value = next;
    notifyListener();
  }

  void bind(StateListener listener) {
    if (this.listener != null && this.listener != listener) {
      throw new IllegalStateException(
          "State is already bound to another open view; initialize @Reactive fields in the menu"
              + " instance instead of sharing a State through a static or injected object");
    }
    this.listener = listener;
  }

  void unbind() {
    this.listener = null;
  }

  private void notifyListener() {
    if (listener == null) {
      return;
    }
    listener.onChange();
  }
}
