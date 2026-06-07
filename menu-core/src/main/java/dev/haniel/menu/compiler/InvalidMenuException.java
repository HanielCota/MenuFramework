package dev.haniel.menu.compiler;

/** Thrown when an annotated class cannot be compiled into a menu template. */
public final class InvalidMenuException extends RuntimeException {

  public InvalidMenuException(String message) {
    super(message);
  }

  public InvalidMenuException(String message, Throwable cause) {
    super(message, cause);
  }
}
