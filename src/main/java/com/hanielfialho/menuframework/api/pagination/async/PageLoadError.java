package com.hanielfialho.menuframework.api.pagination.async;

import java.util.Objects;

/**
 * Bounded, serializable description of a page-load failure.
 *
 * <p>The message can contain database or infrastructure details and should not be displayed
 * directly to a player. Log it for diagnostics and render a generic user-facing error message.
 *
 * @param exceptionType fully qualified exception class name
 * @param message non-blank message containing at most 512 characters
 */
public record PageLoadError(String exceptionType, String message) {

  private static final int MAX_MESSAGE_LENGTH = 512;

  /**
   * Validates and creates the error description.
   *
   * @throws NullPointerException if a component is {@code null}
   * @throws IllegalArgumentException if either component is blank or the message exceeds 512
   *     characters
   */
  public PageLoadError {
    Objects.requireNonNull(exceptionType, "exceptionType");
    Objects.requireNonNull(message, "message");

    if (exceptionType.isBlank()) {
      throw new IllegalArgumentException("exceptionType cannot be blank");
    }

    if (message.isBlank()) {
      throw new IllegalArgumentException("message cannot be blank");
    }

    if (message.length() > MAX_MESSAGE_LENGTH) {
      throw new IllegalArgumentException(
          "message cannot exceed " + MAX_MESSAGE_LENGTH + " characters: " + message.length());
    }
  }

  /**
   * Creates a safe bounded snapshot from a throwable.
   *
   * @param throwable non-null original failure
   * @return summarized failure
   */
  public static PageLoadError from(Throwable throwable) {
    Objects.requireNonNull(throwable, "throwable");

    String rawMessage = throwable.getMessage();
    String message = rawMessage == null || rawMessage.isBlank() ? "No detail message" : rawMessage;

    if (message.length() > MAX_MESSAGE_LENGTH) {
      message = message.substring(0, MAX_MESSAGE_LENGTH);
    }

    return new PageLoadError(throwable.getClass().getName(), message);
  }
}
