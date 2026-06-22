package com.hanielfialho.menuframework.api.pagination;

import java.util.Objects;

/**
 * Immutable request for an offset-based page window.
 *
 * @param cursor zero-based page cursor
 * @param size positive requested entry count
 */
public record PageRequest(PageCursor cursor, int size) {

  /**
   * Validates and creates the request.
   *
   * @throws NullPointerException if {@code cursor} is {@code null}
   * @throws IllegalArgumentException if {@code size} is not positive
   */
  public PageRequest {
    Objects.requireNonNull(cursor, "cursor");

    if (size <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero: " + size);
    }
  }

  /**
   * Creates a request for the first page.
   *
   * @param size positive page size
   * @return first-page request
   */
  public static PageRequest first(int size) {
    return new PageRequest(PageCursor.FIRST, size);
  }

  /**
   * Computes the absolute offset of the first requested entry.
   *
   * @return {@code cursor.index() * size}
   * @throws ArithmeticException if the multiplication overflows a long
   */
  public long offset() {
    return Math.multiplyExact(this.cursor.index(), (long) this.size);
  }
}
