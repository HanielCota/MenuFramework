package dev.haniel.menu.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class InvalidMenuExceptionTest {

  @Test
  void carriesMessageWithoutCause() {
    InvalidMenuException exception = new InvalidMenuException("bad menu");

    assertEquals("bad menu", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void carriesMessageAndCause() {
    Throwable cause = new IllegalStateException("root");
    InvalidMenuException exception = new InvalidMenuException("bad menu", cause);

    assertEquals("bad menu", exception.getMessage());
    assertSame(cause, exception.getCause());
  }
}
