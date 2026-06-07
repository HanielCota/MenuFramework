package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BoundTickTest {

  @Test
  void keepsPeriodAndCallback() {
    int[] runs = {0};
    BoundTick tick = new BoundTick(20, () -> runs[0]++);

    tick.callback().run();

    assertEquals(20, tick.period());
    assertEquals(1, runs[0]);
  }

  @Test
  void rejectsNonPositivePeriod() {
    assertThrows(IllegalArgumentException.class, () -> new BoundTick(0, () -> {}));
    assertThrows(IllegalArgumentException.class, () -> new BoundTick(-1, () -> {}));
  }
}
