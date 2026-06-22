package com.hanielfialho.menuframework.api.pagination.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PageLoadErrorTest {

  @Test
  void extractsThrowableTypeAndMessage() {
    PageLoadError error = PageLoadError.from(new IllegalStateException("query failed"));

    assertEquals(IllegalStateException.class.getName(), error.exceptionType());
    assertEquals("query failed", error.message());
  }

  @Test
  void replacesMissingThrowableMessage() {
    PageLoadError error = PageLoadError.from(new IllegalStateException());

    assertEquals("No detail message", error.message());
  }

  @Test
  void truncatesTechnicalMessage() {
    PageLoadError error = PageLoadError.from(new RuntimeException("x".repeat(700)));

    assertEquals(512, error.message().length());
    assertTrue(error.message().chars().allMatch(character -> character == 'x'));
  }

  @Test
  void rejectsDirectMessagesLongerThanTheDocumentedLimit() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new PageLoadError(RuntimeException.class.getName(), "x".repeat(513)));
  }

  @Test
  void rejectsBlankFields() {
    assertThrows(IllegalArgumentException.class, () -> new PageLoadError(" ", "message"));

    assertThrows(IllegalArgumentException.class, () -> new PageLoadError("type", " "));
  }
}
