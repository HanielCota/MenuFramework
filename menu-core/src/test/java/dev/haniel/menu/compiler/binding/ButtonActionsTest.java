package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ButtonActionsTest {

  @Test
  void invokesHandleWithResolvedArguments() throws ReflectiveOperationException {
    AtomicReference<Object[]> received = new AtomicReference<>();
    Recorder recorder = new Recorder(received);
    MethodHandle bound =
        MethodHandles.lookup()
            .unreflect(Recorder.class.getDeclaredMethod("run", ClickContext.class))
            .bindTo(recorder);
    ClickContext context = context();
    ButtonArguments arguments = ctx -> new Object[] {ctx};

    MenuAction action = ButtonActions.bind(bound, arguments);
    action.onClick(context);

    assertArrayEquals(new Object[] {context}, received.get());
  }

  @Test
  void passesNoArgumentsToNoArgHandle() throws ReflectiveOperationException {
    AtomicReference<Object[]> received = new AtomicReference<>();
    Recorder recorder = new Recorder(received);
    MethodHandle bound =
        MethodHandles.lookup()
            .unreflect(Recorder.class.getDeclaredMethod("noArgs"))
            .bindTo(recorder);

    ButtonActions.bind(bound, ctx -> new Object[0]).onClick(context());

    assertArrayEquals(new Object[0], received.get());
  }

  @Test
  void wrapsThrowingHandleInMenuActionException() throws ReflectiveOperationException {
    MethodHandle bound =
        MethodHandles.lookup()
            .unreflect(Recorder.class.getDeclaredMethod("explode"))
            .bindTo(new Recorder(new AtomicReference<>()));
    MenuAction action = ButtonActions.bind(bound, ctx -> new Object[0]);

    MenuActionException error =
        assertThrows(MenuActionException.class, () -> action.onClick(context()));
    assertInstanceOf(IllegalStateException.class, error.getCause());
  }

  @Test
  void propagatesErrorWithoutWrapping() throws ReflectiveOperationException {
    MethodHandle bound =
        MethodHandles.lookup()
            .unreflect(Recorder.class.getDeclaredMethod("fail"))
            .bindTo(new Recorder(new AtomicReference<>()));
    MenuAction action = ButtonActions.bind(bound, ctx -> new Object[0]);

    AssertionError error = assertThrows(AssertionError.class, () -> action.onClick(context()));
    assertEquals("boom", error.getMessage());
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

  static final class Recorder {

    private final AtomicReference<Object[]> received;

    Recorder(AtomicReference<Object[]> received) {
      this.received = received;
    }

    void run(ClickContext context) {
      received.set(new Object[] {context});
    }

    void noArgs() {
      received.set(new Object[0]);
    }

    void explode() {
      throw new IllegalStateException("boom");
    }

    void fail() {
      throw new AssertionError("boom");
    }
  }
}
