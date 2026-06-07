package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Adversarial wrapping checks for {@link ButtonActions}: the exact cause must be preserved (no
 * double-wrapping, no lost cause), checked exceptions must be wrapped not swallowed, and {@link
 * Error} must propagate untouched.
 */
class ButtonActionsExceptionEdgeCasesTest {

  @Test
  void preservesTheOriginalCauseInstanceWithoutDoubleWrapping()
      throws ReflectiveOperationException {
    MenuAction action = ButtonActions.bind(bound("throwChecked"), ctx -> new Object[0]);

    MenuActionException error =
        assertThrows(MenuActionException.class, () -> action.onClick(context()));

    assertSame(Thrower.CHECKED, error.getCause(), "the exact thrown instance must be the cause");
    assertNull(error.getCause().getCause(), "no extra wrapping layer should be inserted");
  }

  @Test
  void wrapsRuntimeExceptionRatherThanLettingItEscapeRaw() throws ReflectiveOperationException {
    MenuAction action = ButtonActions.bind(bound("throwRuntime"), ctx -> new Object[0]);

    MenuActionException error =
        assertThrows(MenuActionException.class, () -> action.onClick(context()));
    assertSame(Thrower.RUNTIME, error.getCause());
  }

  @Test
  void propagatesErrorUnwrapped() throws ReflectiveOperationException {
    MenuAction action = ButtonActions.bind(bound("throwError"), ctx -> new Object[0]);

    OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> action.onClick(context()));
    assertSame(Thrower.OOM, error, "an Error must propagate as-is, never wrapped");
  }

  private static MethodHandle bound(String method) throws ReflectiveOperationException {
    return MethodHandles.lookup()
        .unreflect(Thrower.class.getDeclaredMethod(method))
        .bindTo(new Thrower());
  }

  private static ClickContext context() {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }

  @SuppressWarnings("unused")
  static final class Thrower {
    static final Exception CHECKED = new Exception("checked boom");
    static final RuntimeException RUNTIME = new IllegalStateException("runtime boom");
    static final OutOfMemoryError OOM = new OutOfMemoryError("oom boom");

    void throwChecked() throws Exception {
      throw CHECKED;
    }

    void throwRuntime() {
      throw RUNTIME;
    }

    void throwError() {
      throw OOM;
    }
  }
}
