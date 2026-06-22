package com.hanielfialho.menuframework.api.pagination;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Synchronous page source backed by an immutable collection snapshot.
 *
 * @param <T> entry type
 */
public final class Paginator<T> {

  private final List<T> elements;

  private Paginator(Collection<? extends T> elements) {
    this.elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
  }

  /**
   * Copies a collection into an immutable paginator.
   *
   * @param elements collection containing no null elements
   * @param <T> entry type
   * @return paginator independent from the original collection
   * @throws NullPointerException if the collection or one of its elements is {@code null}
   */
  public static <T> Paginator<T> copyOf(Collection<? extends T> elements) {
    return new Paginator<>(elements);
  }

  /**
   * Returns the total number of entries.
   *
   * @return total entry count
   */
  public int totalElements() {
    return this.elements.size();
  }

  /**
   * Returns the complete immutable snapshot.
   *
   * @return immutable entry list
   */
  public List<T> elements() {
    return this.elements;
  }

  /**
   * Computes the number of pages for a page size.
   *
   * <p>An empty collection exposes one virtual page so a menu can render an explicit empty state.
   *
   * @param pageSize positive page size
   * @return at least one page
   * @throws IllegalArgumentException if {@code pageSize} is not positive
   */
  public int pageCount(int pageSize) {
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero: " + pageSize);
    }

    if (this.elements.isEmpty()) {
      return 1;
    }

    return ((this.elements.size() - 1) / pageSize) + 1;
  }

  /**
   * Returns a validated page with a known total count.
   *
   * <p>A cursor beyond the final page is normalized to the final existing page.
   *
   * @param requested non-null request
   * @return immutable page slice
   * @throws NullPointerException if {@code requested} is {@code null}
   */
  public PageSlice<T> page(PageRequest requested) {
    Objects.requireNonNull(requested, "requested");

    int pageCount = this.pageCount(requested.size());
    int normalizedIndex = Math.min(requested.cursor().index(), pageCount - 1);

    PageRequest normalizedRequest =
        new PageRequest(PageCursor.of(normalizedIndex), requested.size());

    int fromIndex = Math.toIntExact(normalizedRequest.offset());
    int toIndex = (int) Math.min(this.elements.size(), (long) fromIndex + normalizedRequest.size());

    return PageSlice.knownTotal(
        normalizedRequest, this.elements.subList(fromIndex, toIndex), this.elements.size());
  }
}
