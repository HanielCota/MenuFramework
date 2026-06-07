package dev.haniel.menu.state;

/** Notified when an observed {@link State} changes value. Implemented by the reactive view. */
@FunctionalInterface
public interface StateListener {

  /** Called after a bound state changed to a new, different value. */
  void onChange();
}
