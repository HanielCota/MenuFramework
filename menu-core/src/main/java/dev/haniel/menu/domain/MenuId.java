package dev.haniel.menu.domain;

import java.util.regex.Pattern;

/**
 * The logical identifier of a menu.
 *
 * <p>Restricted to lowercase letters, digits, underscore and hyphen so the id can be turned into a
 * {@code <id>.yml} file name without ever escaping its menus directory.
 *
 * @param value the id; must match {@code [a-z0-9_-]+} and be at most {@value #MAX_LENGTH}
 *     characters
 */
public record MenuId(String value) {

  private static final int MAX_LENGTH = 64;
  private static final Pattern SAFE = Pattern.compile("[a-z0-9_-]+");

  public MenuId {
    if (value == null || !SAFE.matcher(value).matches()) {
      throw new IllegalArgumentException("MenuId must match [a-z0-9_-]+, was: " + value);
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "MenuId must be at most " + MAX_LENGTH + " characters, was: " + value.length());
    }
  }
}
