package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.MenuAction;
import java.lang.invoke.MethodHandle;

/**
 * A boot-resolved, instance-free {@code @Button} handle, bound to an instance per open.
 *
 * @see #bind(Object)
 */
public final class UnboundAction {

  private final MethodHandle handle;
  private final ButtonArguments arguments;

  /**
   * Wraps an unbound handle and the argument supplier resolved for its parameter at boot.
   *
   * @param handle the unbound method handle; never null
   * @param arguments the per-click argument supplier for the method; never null
   */
  public UnboundAction(MethodHandle handle, ButtonArguments arguments) {
    this.handle = handle;
    this.arguments = arguments;
  }

  /**
   * Binds the handle to the given instance, yielding a runnable action.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound action
   */
  public MenuAction bind(Object instance) {
    return ButtonActions.bind(handle.bindTo(instance), arguments);
  }
}
