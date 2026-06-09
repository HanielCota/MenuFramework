package dev.haniel.menu.config;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.Slot;

/**
 * Validates a parsed {@link MenuConfig} against the framework's structural rules.
 *
 * <p>Separated from {@link MenuLoader} so loading owns only IO, parsing and caching while the
 * structural rules — button slots within bounds, a resolvable pagination mask — live in one place.
 * Every failure surfaces as an {@link InvalidMenuException} aimed at the server owner.
 */
final class MenuConfigValidator {

  private MenuConfigValidator() {}

  /**
   * Validates the given configuration for the named menu.
   *
   * @param id the menu id, used only for error messages; never null
   * @param config the parsed configuration; never null
   * @throws InvalidMenuException if a button slot is out of bounds or the pagination mask is
   *     invalid
   */
  static void validate(MenuId id, MenuConfig config) {
    validateButtons(id, config);
    config.paginationConfig().ifPresent(pagination -> validatePagination(id, config, pagination));
  }

  private static void validateButtons(MenuId id, MenuConfig config) {
    config
        .buttons()
        .forEach((buttonId, button) -> validateSlot(id, buttonId, button.slot(), config.rows()));
  }

  private static void validatePagination(
      MenuId id, MenuConfig config, PaginationConfig pagination) {
    try {
      MaskLayout.resolve(pagination.mask(), config.rows());
    } catch (RuntimeException exception) {
      throw new InvalidMenuException(
          "Menu '" + id.value() + "' has invalid pagination mask: " + exception.getMessage(),
          exception);
    }
  }

  private static void validateSlot(MenuId id, String buttonId, int slot, int rows) {
    try {
      Slot.of(slot, rows);
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "Menu '"
              + id.value()
              + "' button '"
              + buttonId
              + "' has invalid slot: "
              + exception.getMessage(),
          exception);
    }
  }
}
