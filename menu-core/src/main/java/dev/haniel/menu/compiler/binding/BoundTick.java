package dev.haniel.menu.compiler.binding;

/**
 * A {@code @Tick} method bound to a per-player instance, ready to schedule.
 *
 * <p>Appearance-free behaviour: the platform layer schedules {@link #callback()} every {@link
 * #period()} ticks on the view's owning thread.
 *
 * @param period the period in ticks; must be {@code >= 1}
 * @param callback the bound invocation to run each period
 */
public record BoundTick(long period, Runnable callback) {

  public BoundTick {
    if (period < 1) {
      throw new IllegalArgumentException("tick period must be >= 1 but was " + period);
    }
  }
}
