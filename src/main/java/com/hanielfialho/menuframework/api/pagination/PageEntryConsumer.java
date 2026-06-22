package com.hanielfialho.menuframework.api.pagination;

/**
 * Consumer for an entry positioned in a {@link PaginationLayout}.
 *
 * @param <T> entry type
 */
@FunctionalInterface
public interface PageEntryConsumer<T> {

  /**
   * Consumes one entry and its relative and absolute positions.
   *
   * @param slot corresponding menu slot
   * @param entry current non-null entry
   * @param indexInPage zero-based index inside the page
   * @param absoluteIndex zero-based index in the complete result set
   */
  void accept(int slot, T entry, int indexInPage, long absoluteIndex);
}
