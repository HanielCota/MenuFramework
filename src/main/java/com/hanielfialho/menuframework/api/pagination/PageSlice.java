package com.hanielfialho.menuframework.api.pagination;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Immutable result of a page request.
 *
 * @param <T> entry type
 */
public final class PageSlice<T> {

  private static final long UNKNOWN_TOTAL_ELEMENTS = -1L;

  private final PageRequest request;
  private final List<T> entries;
  private final boolean hasNext;
  private final long totalElements;

  private PageSlice(
      PageRequest request, Collection<? extends T> entries, boolean hasNext, long totalElements) {
    this.request = Objects.requireNonNull(request, "request");
    this.entries = List.copyOf(Objects.requireNonNull(entries, "entries"));

    if (this.entries.size() > request.size()) {
      throw new IllegalArgumentException(
          "Page contains more entries than requested: "
              + this.entries.size()
              + " > "
              + request.size());
    }

    this.hasNext = hasNext;
    this.totalElements = totalElements;
  }

  /**
   * Creates a page whose total number of entries is known.
   *
   * <p>The supplied entries must represent the complete requested window. This strict validation
   * prevents inconsistent offsets and skipped entries when callers provide a total count that does
   * not match the page data.
   *
   * @param request page request
   * @param entries complete entries for the requested window
   * @param totalElements total number of matching entries
   * @param <T> entry type
   * @return validated immutable page
   */
  public static <T> PageSlice<T> knownTotal(
      PageRequest request, Collection<? extends T> entries, long totalElements) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(entries, "entries");

    if (totalElements < 0L) {
      throw new IllegalArgumentException("Total elements must be >= 0: " + totalElements);
    }

    List<T> snapshot = List.copyOf(entries);
    long offset = request.offset();

    if (totalElements == 0L) {
      if (offset != 0L || !snapshot.isEmpty()) {
        throw new IllegalArgumentException("An empty result only has the first empty page");
      }

      return new PageSlice<>(request, snapshot, false, 0L);
    }

    if (offset >= totalElements) {
      throw new IllegalArgumentException(
          "Page offset is outside the known result set: " + offset + " >= " + totalElements);
    }

    long remaining = totalElements - offset;
    int expectedEntries = (int) Math.min(request.size(), remaining);

    if (snapshot.size() != expectedEntries) {
      throw new IllegalArgumentException(
          "Known-total page must contain the complete requested "
              + "window: expected "
              + expectedEntries
              + " entries but received "
              + snapshot.size());
    }

    boolean hasNext = remaining > request.size();

    return new PageSlice<>(request, snapshot, hasNext, totalElements);
  }

  /**
   * Creates a page without an overall count.
   *
   * <p>When {@code hasNext} is {@code true}, the page must be full. A partially filled page
   * followed by another page would make offset-based pagination skip entries.
   *
   * @param request page request
   * @param entries page entries
   * @param hasNext whether another page is known to exist
   * @param <T> entry type
   * @return validated immutable page
   */
  public static <T> PageSlice<T> unknownTotal(
      PageRequest request, Collection<? extends T> entries, boolean hasNext) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(entries, "entries");

    List<T> snapshot = List.copyOf(entries);

    if (hasNext && snapshot.size() != request.size()) {
      throw new IllegalArgumentException(
          "A page with hasNext=true must contain exactly "
              + request.size()
              + " entries: "
              + snapshot.size());
    }

    return new PageSlice<>(request, snapshot, hasNext, UNKNOWN_TOTAL_ELEMENTS);
  }

  /**
   * Returns the request that produced this page.
   *
   * @return request
   */
  public PageRequest request() {
    return this.request;
  }

  /**
   * Returns the zero-based cursor.
   *
   * @return cursor
   */
  public PageCursor cursor() {
    return this.request.cursor();
  }

  /**
   * Returns the requested page size.
   *
   * @return page size
   */
  public int pageSize() {
    return this.request.size();
  }

  /**
   * Returns an immutable entry list.
   *
   * @return entries
   */
  public List<T> entries() {
    return this.entries;
  }

  /**
   * Returns whether a previous cursor exists.
   *
   * @return {@code true} when this is not the first page
   */
  public boolean hasPrevious() {
    return this.cursor().index() > 0;
  }

  /**
   * Returns whether another page is known to exist.
   *
   * @return next-page flag
   */
  public boolean hasNext() {
    return this.hasNext;
  }

  /**
   * Returns whether the page has no entries.
   *
   * @return empty flag
   */
  public boolean isEmpty() {
    return this.entries.isEmpty();
  }

  /**
   * Returns the one-based page number intended for display.
   *
   * @return one-based page number
   */
  public long number() {
    return this.cursor().number();
  }

  /**
   * Returns the total number of entries when known.
   *
   * @return optional total entry count
   */
  public OptionalLong totalElements() {
    return this.totalElements == UNKNOWN_TOTAL_ELEMENTS
        ? OptionalLong.empty()
        : OptionalLong.of(this.totalElements);
  }

  /**
   * Returns the total page count when the total entry count is known. Empty result sets expose one
   * virtual page.
   *
   * @return optional total page count
   */
  public OptionalLong totalPages() {
    if (this.totalElements == UNKNOWN_TOTAL_ELEMENTS) {
      return OptionalLong.empty();
    }

    long total = this.totalElements;
    long pages = total == 0L ? 1L : ((total - 1L) / this.pageSize()) + 1L;

    return OptionalLong.of(pages);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "PageSlice{"
        + "request="
        + this.request
        + ", entries="
        + this.entries.size()
        + ", hasNext="
        + this.hasNext
        + ", totalElements="
        + this.totalElements()
        + '}';
  }
}
