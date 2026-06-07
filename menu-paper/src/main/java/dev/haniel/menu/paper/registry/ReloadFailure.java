package dev.haniel.menu.paper.registry;

import dev.haniel.menu.domain.MenuId;

/**
 * Describes one menu reload failure.
 *
 * @param id the menu that failed
 * @param message the human-readable failure reason; never null
 */
public record ReloadFailure(MenuId id, String message) {

  /**
   * Builds a failure from a thrown cause, deriving a non-null message even when the cause carries
   * none (for example a bare {@link NullPointerException}).
   *
   * @param id the menu that failed; never null
   * @param cause the thrown failure; never null
   * @return the described failure
   */
  public static ReloadFailure from(MenuId id, Throwable cause) {
    String message = cause.getMessage();
    return new ReloadFailure(id, message != null ? message : cause.getClass().getSimpleName());
  }
}
