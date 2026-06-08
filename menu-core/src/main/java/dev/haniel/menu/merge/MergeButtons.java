package dev.haniel.menu.merge;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.Slot;

/**
 * Shared button-to-slot resolution and presence errors for the static and paginated mergers, so a
 * misplaced or unconfigured {@code @Button} fails the same way on both paths.
 */
final class MergeButtons {

  private MergeButtons() {}

  /**
   * Resolves a button's slot against the menu bounds.
   *
   * @param button the configured button; never null
   * @param config the menu appearance; never null
   * @return the validated slot index
   * @throws InvalidMenuException if the slot falls outside the menu bounds
   */
  static int slot(ButtonConfig button, MenuConfig config) {
    try {
      return Slot.of(button.slot(), config.rows()).value();
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "Button slot " + button.slot() + " is outside the menu bounds", exception);
    }
  }

  /**
   * Builds the error for an annotated button with no matching YAML entry.
   *
   * @param id the button id; never null
   * @return the descriptive failure
   */
  static InvalidMenuException missingButton(String id) {
    return new InvalidMenuException(
        "Button '" + id + "' is annotated but missing in YAML; add buttons." + id);
  }
}
