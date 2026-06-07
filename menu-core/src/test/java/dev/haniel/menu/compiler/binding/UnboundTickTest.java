package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.MenuActionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.junit.jupiter.api.Test;

class UnboundTickTest {

  @Test
  void bindInvokesTheMethodOnTheInstance() throws ReflectiveOperationException {
    Recorder recorder = new Recorder();
    BoundTick bound = new UnboundTick(handle("run"), 20).bind(recorder);

    bound.callback().run();

    assertEquals(1, recorder.runs);
    assertEquals(20, bound.period());
  }

  @Test
  void wrapsAFailingMethodInMenuActionException() throws ReflectiveOperationException {
    BoundTick bound = new UnboundTick(handle("boom"), 5).bind(new Recorder());

    assertThrows(MenuActionException.class, () -> bound.callback().run());
  }

  private static MethodHandle handle(String name) throws ReflectiveOperationException {
    return MethodHandles.lookup().unreflect(Recorder.class.getDeclaredMethod(name));
  }

  static final class Recorder {
    int runs;

    void run() {
      runs++;
    }

    void boom() {
      throw new IllegalStateException("boom");
    }
  }
}
