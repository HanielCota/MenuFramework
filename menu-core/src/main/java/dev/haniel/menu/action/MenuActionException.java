package dev.haniel.menu.action;

/** Thrown when a button action fails while handling a click. */
public final class MenuActionException extends RuntimeException {

  public MenuActionException(String message, Throwable cause) {
    super(message, cause);
  }
}
