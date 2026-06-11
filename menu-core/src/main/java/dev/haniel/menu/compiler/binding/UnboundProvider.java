package dev.haniel.menu.compiler.binding;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * A boot-resolved, instance-free {@code @Paginated} handle, bound to an instance per open.
 *
 * @see #bind(Object)
 */
public final class UnboundProvider implements UnboundContent {

  private final MethodHandle handle;

  /**
   * Wraps an unbound handle of type {@code (Instance) -> List<MenuItem>}.
   *
   * @param handle the unbound method handle; never null
   */
  public UnboundProvider(MethodHandle handle) {
    this.handle = Objects.requireNonNull(handle, "handle");
  }

  /**
   * Binds the handle to the given instance, yielding a content provider.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound content provider
   */
  @Override
  public ContentProvider bind(Object instance) {
    return new ContentProvider(handle.bindTo(instance));
  }
}
