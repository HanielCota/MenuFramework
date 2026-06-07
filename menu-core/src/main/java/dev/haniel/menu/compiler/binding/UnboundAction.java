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
  private final ButtonGuards guards;

  /**
   * Wraps an unbound handle and the argument supplier resolved for its parameter at boot.
   *
   * @param handle the unbound method handle; never null
   * @param arguments the per-click argument supplier for the method; never null
   */
  public UnboundAction(MethodHandle handle, ButtonArguments arguments) {
    this(handle, arguments, ButtonGuards.none());
  }

  /**
   * Wraps an unbound handle guarded by a permission and/or cooldown.
   *
   * @param handle the unbound method handle; never null
   * @param arguments the per-click argument supplier for the method; never null
   * @param guards the permission and cooldown rules; never null
   */
  public UnboundAction(MethodHandle handle, ButtonArguments arguments, ButtonGuards guards) {
    this.handle = handle;
    this.arguments = arguments;
    this.guards = guards;
  }

  /**
   * Binds the handle to the given instance, yielding a runnable action.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound action, decorated by its guards
   */
  public MenuAction bind(Object instance) {
    return guards.apply(ButtonActions.bind(handle.bindTo(instance), arguments));
  }
}
