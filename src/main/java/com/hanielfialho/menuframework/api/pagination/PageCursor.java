package com.hanielfialho.menuframework.api.pagination;

/**
 * Immutable cursor for index-based pagination.
 *
 * @param index zero-based page index
 */
public record PageCursor(int index) {

  /** Cursor representing the first page. */
  public static final PageCursor FIRST = new PageCursor(0);

  /**
   * Validates and creates the cursor.
   *
   * @throws IllegalArgumentException if {@code index} is negative
   */
  public PageCursor {
    if (index < 0) {
      throw new IllegalArgumentException("Page index must be >= 0: " + index);
    }
  }

  /**
   * Creates a cursor, reusing {@link #FIRST} for index zero.
   *
   * @param index zero-based page index
   * @return validated cursor
   */
  public static PageCursor of(int index) {
    return index == 0 ? FIRST : new PageCursor(index);
  }

  /**
   * Returns the one-based page number intended for display.
   *
   * @return {@code index + 1}
   */
  public long number() {
    return this.index + 1L;
  }

  /**
   * Returns the previous cursor, or this cursor when already at the first page.
   *
   * @return normalized previous cursor
   */
  public PageCursor previous() {
    return this.index == 0 ? this : PageCursor.of(this.index - 1);
  }

  /**
   * Returns the next cursor.
   *
   * @return next cursor
   * @throws ArithmeticException if the integer index overflows
   */
  public PageCursor next() {
    return PageCursor.of(Math.incrementExact(this.index));
  }
}
