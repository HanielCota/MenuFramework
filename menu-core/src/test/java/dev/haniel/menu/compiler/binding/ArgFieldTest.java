package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ArgFieldTest {

  @Test
  void writesAssignableValueIntoField() throws ReflectiveOperationException {
    Holder holder = new Holder();

    stringField().inject(holder, "payload");

    assertEquals("payload", holder.target);
  }

  @Test
  void acceptsAssignableNonNullValuesOnly() throws ReflectiveOperationException {
    ArgField field = stringField();

    assertTrue(field.accepts("text"));
    assertFalse(field.accepts(1), "a value of the wrong type must not be accepted");
    assertFalse(field.accepts(null), "a missing value must not be accepted");
  }

  @Test
  void wrapsWriteFailureAsInvalidMenu() throws ReflectiveOperationException {
    ArgField field = stringField();

    assertThrows(InvalidMenuException.class, () -> field.inject(new Holder(), 42));
  }

  private static ArgField stringField() throws ReflectiveOperationException {
    Field field = Holder.class.getDeclaredField("target");
    field.setAccessible(true);
    MethodHandle setter = MethodHandles.lookup().unreflectSetter(field);
    return new ArgField("target", String.class, setter);
  }

  static final class Holder {

    String target;
  }
}
