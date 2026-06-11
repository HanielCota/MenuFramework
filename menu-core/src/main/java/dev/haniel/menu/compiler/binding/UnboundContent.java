package dev.haniel.menu.compiler.binding;

/**
 * A boot-resolved, instance-free content source, bound to a fresh instance per open.
 *
 * <p>Either an {@link UnboundProvider} (eager full list) or an {@link UnboundPageProvider} (lazy
 * per-page). {@link #bind(Object)} yields the matching {@link BoundContent}.
 */
public sealed interface UnboundContent permits UnboundProvider, UnboundPageProvider {

  /**
   * Binds the handle to the given instance, yielding a per-instance content source.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound content source
   */
  BoundContent bind(Object instance);
}
