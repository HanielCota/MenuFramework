package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.junit.jupiter.api.Test;

class InstantiatorTest {

  @Test
  void createsFromSupplier() {
    assertEquals("instance", new Instantiator(() -> "instance").create());
  }

  @Test
  void rejectsNullResult() {
    Instantiator instantiator = new Instantiator(() -> null);

    assertThrows(NullPointerException.class, instantiator::create);
  }

  @Test
  void rethrowsRuntimeFailureAsIs() {
    Instantiator instantiator =
        new Instantiator(
            () -> {
              throw new IllegalStateException("boom");
            });

    assertThrows(IllegalStateException.class, instantiator::create);
  }

  @Test
  void buildsFromConstructorHandle() throws ReflectiveOperationException {
    MethodHandle constructor =
        MethodHandles.lookup().findConstructor(Sample.class, MethodType.methodType(void.class));

    assertInstanceOf(Sample.class, new Instantiator(constructor).create());
  }

  static final class Sample {}
}
