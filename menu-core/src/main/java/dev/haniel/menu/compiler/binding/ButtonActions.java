package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.click.ClickContext;
import java.lang.invoke.MethodHandle;

/**
 * Adapts a bound {@code @Button} handle into a {@link MenuAction}, supplying its arguments.
 *
 * <p>The one place that invokes a button handle, shared by the static and paginated paths so both
 * support the same parameter shapes. The argument shape is captured by the {@link ButtonArguments}
 * resolved at boot. A failing button surfaces as a {@link MenuActionException} rather than a
 * swallowed {@code Throwable}.
 */
public final class ButtonActions {

  private ButtonActions() {}

  /**
   * Builds the action that runs the given bound handle, resolving its arguments per click.
   *
   * @param bound the handle already bound to the menu instance; never null
   * @param arguments the per-click argument supplier resolved at boot; never null
   * @return the runnable action
   */
  public static MenuAction bind(MethodHandle bound, ButtonArguments arguments) {
    return context -> invoke(bound, arguments, context);
  }

  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  private static void invoke(MethodHandle bound, ButtonArguments arguments, ClickContext context) {
    try {
      bound.invokeWithArguments(arguments.forContext(context));
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new MenuActionException("Button action failed", throwable);
    }
  }
}
