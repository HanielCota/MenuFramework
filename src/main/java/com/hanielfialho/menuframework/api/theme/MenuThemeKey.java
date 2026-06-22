package com.hanielfialho.menuframework.api.theme;

import java.util.Objects;

/** Stable identifier of a themed menu icon. */
public record MenuThemeKey(String value) {

  /**
   * Validates the identifier.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if it is blank or padded with whitespace
   */
  public MenuThemeKey {
    Objects.requireNonNull(value, "value");

    if (value.isBlank()) {
      throw new IllegalArgumentException("Theme key cannot be blank");
    }

    if (!value.equals(value.strip())) {
      throw new IllegalArgumentException(
          "Theme key cannot contain leading or trailing whitespace: '" + value + "'");
    }
  }

  /**
   * Creates a key.
   *
   * @param value stable identifier
   * @return validated key
   */
  public static MenuThemeKey of(String value) {
    return new MenuThemeKey(value);
  }
}
