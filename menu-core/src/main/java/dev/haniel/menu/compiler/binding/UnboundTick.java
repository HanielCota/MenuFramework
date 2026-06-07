package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuActionException;
import java.lang.invoke.MethodHandle;

/**
 * A boot-resolved, instance-free {@code @Tick} handle, bound to an instance per open.
 *
 * @see #bind(Object)
 */
public final class UnboundTick {

  private final MethodHandle handle;
  private final long period;

  /**
   * Wraps an unbound no-arg handle and its period.
   *
   * @param handle the unbound method handle; never null
   * @param period the period in ticks; must be {@code >= 1}
   */
  public UnboundTick(MethodHandle handle, long period) {
    this.handle = handle;
    this.period = period;
  }

  /**
   * Binds the handle to the given instance, yielding a schedulable tick.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound tick
   */
  public BoundTick bind(Object instance) {
    MethodHandle bound = handle.bindTo(instance);
    return new BoundTick(period, () -> invoke(bound));
  }

  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  private static void invoke(MethodHandle bound) {
    try {
      bound.invoke();
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new MenuActionException("Tick action failed", throwable);
    }
  }
}
