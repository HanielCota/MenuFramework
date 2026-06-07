package dev.haniel.menu.domain;

/**
 * The logical identifier of a button.
 *
 * @param value the id; must be non-null and non-blank
 */
public record ButtonId(String value) {

  public ButtonId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ButtonId cannot be blank");
    }
  }
}
