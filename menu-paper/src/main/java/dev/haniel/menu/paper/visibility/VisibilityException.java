package dev.haniel.menu.paper.visibility;

/**
 * Thrown when a {@code @Visible} rule throws while deciding whether a button is shown.
 *
 * <p>Carries the original failure as its cause so the menu listener can log it like any other
 * action error, rather than letting a raw {@code Throwable} from a method handle escape.
 */
public final class VisibilityException extends RuntimeException {

  /**
   * Creates the exception with a message and the rule's failure.
   *
   * @param message what failed
   * @param cause the throwable the rule raised
   */
  public VisibilityException(String message, Throwable cause) {
    super(message, cause);
  }
}
