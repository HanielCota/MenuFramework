package dev.haniel.menu.compiler.binding;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * A boot-resolved, instance-free lazy {@code @Paginated} handle, bound to an instance per open.
 *
 * @see #bind(Object)
 */
public final class UnboundPageProvider implements UnboundContent {

  private final MethodHandle handle;

  /**
   * Wraps an unbound handle of type {@code (Instance, int, int) -> Page<MenuItem>}.
   *
   * @param handle the unbound method handle; never null
   */
  public UnboundPageProvider(MethodHandle handle) {
    this.handle = Objects.requireNonNull(handle, "handle");
  }

  @Override
  public PageProvider bind(Object instance) {
    return new PageProvider(handle.bindTo(instance));
  }
}
