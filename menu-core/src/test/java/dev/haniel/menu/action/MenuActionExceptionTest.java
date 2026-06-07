package dev.haniel.menu.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class MenuActionExceptionTest {

  @Test
  void carriesMessageAndCause() {
    Throwable cause = new IllegalStateException("root");
    MenuActionException exception = new MenuActionException("button failed", cause);

    assertEquals("button failed", exception.getMessage());
    assertSame(cause, exception.getCause());
  }
}
