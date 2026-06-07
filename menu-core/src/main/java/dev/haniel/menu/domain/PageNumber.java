package dev.haniel.menu.domain;

/**
 * A zero-based page index.
 *
 * @param value the page index; must be zero or positive
 */
public record PageNumber(int value) {

  public PageNumber {
    if (value < 0) {
      throw new IllegalArgumentException("page must be >= 0 but was " + value);
    }
  }

  /**
   * Returns the first page.
   *
   * @return page zero
   */
  public static PageNumber first() {
    return new PageNumber(0);
  }

  /**
   * Returns the next page, clamped at the last representable page.
   *
   * @return the page after this one, or this page if already at {@link Integer#MAX_VALUE}
   */
  public PageNumber next() {
    return value == Integer.MAX_VALUE ? this : new PageNumber(value + 1);
  }

  /**
   * Returns the previous page, clamped at the first page.
   *
   * @return the page before this one, or this page if already first
   */
  public PageNumber previous() {
    return value == 0 ? this : new PageNumber(value - 1);
  }
}
