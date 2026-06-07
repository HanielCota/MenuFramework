package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.state.State;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.junit.jupiter.api.Test;

class StateFieldTest {

  @Test
  void readsStateFromInstance() throws ReflectiveOperationException {
    Sample sample = new Sample();
    StateField field = new StateField("visible", getterOf("visible"));

    assertSame(sample.visible, field.read(sample));
  }

  @Test
  void failsNamingTheFieldWhenStateIsNull() throws ReflectiveOperationException {
    StateField field = new StateField("missing", getterOf("missing"));

    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> field.read(new Sample()));
    assertTrue(error.getMessage().contains("missing"));
  }

  @Test
  void wrapsGetterFailureNamingTheField() throws ReflectiveOperationException {
    MethodHandle broken = MethodHandles.lookup().unreflect(Sample.class.getDeclaredMethod("broken"));
    StateField field = new StateField("broken", broken);

    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> field.read(new Sample()));
    assertTrue(error.getMessage().contains("broken"));
  }

  private static MethodHandle getterOf(String name) throws ReflectiveOperationException {
    return MethodHandles.lookup().unreflectGetter(Sample.class.getDeclaredField(name));
  }

  static final class Sample {
    final State<String> visible = State.of("hello");
    final State<String> missing = null;

    State<String> broken() {
      throw new IllegalStateException("boom");
    }
  }
}
