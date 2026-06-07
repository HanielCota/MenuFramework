package dev.haniel.menu.config;

import java.util.Map;
import java.util.Optional;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * The appearance of a menu, read from YAML. Behaviour stays in the annotated class.
 *
 * <p>Buttons are keyed by their logical id, matching {@code @Button(id = ...)} at merge time.
 *
 * @param title the MiniMessage title; defaults to empty
 * @param rows the number of rows; must be in {@code 1..6}
 * @param buttons the buttons by logical id; defaults to empty
 * @param pagination the pagination appearance, or {@code null} for a static menu
 */
@ConfigSerializable
public record MenuConfig(
    String title, int rows, Map<String, ButtonConfig> buttons, PaginationConfig pagination) {

  public MenuConfig {
    if (rows < 1 || rows > 6) {
      throw new IllegalArgumentException("rows must be between 1 and 6 but was " + rows);
    }
    title = (title == null) ? "" : title;
    buttons = (buttons == null) ? Map.of() : Map.copyOf(buttons);
  }

  /**
   * Returns the pagination appearance, if this menu is paginated.
   *
   * @return the pagination config, or empty for a static menu
   */
  public Optional<PaginationConfig> paginationConfig() {
    if (pagination == null || pagination.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(pagination);
  }

  /**
   * Returns the total number of slots in this menu.
   *
   * @return {@code rows * 9}
   */
  public int size() {
    return rows * 9;
  }
}
