package dev.haniel.menu.domain;

import dev.haniel.menu.item.MenuItem;
import java.util.List;

/**
 * A first-class collection over the paginated items, slicing them into pages.
 *
 * <p>The page capacity ({@code perPage}) is the number of content slots in the mask and is passed
 * per call rather than stored, so the collection holds only the items.
 */
public final class Paginator {

  private final List<MenuItem> items;

  /**
   * Creates a paginator over a defensive copy of the given items.
   *
   * @param items the full, unpaginated list; never null
   */
  public Paginator(List<MenuItem> items) {
    this.items = List.copyOf(items);
  }

  /**
   * Returns the total number of pages, never less than one.
   *
   * @param perPage the page capacity; must be positive
   * @return the page count
   */
  public int totalPages(int perPage) {
    requirePositive(perPage);
    if (items.isEmpty()) {
      return 1;
    }
    return (items.size() + perPage - 1) / perPage;
  }

  private static void requirePositive(int perPage) {
    if (perPage < 1) {
      throw new IllegalArgumentException("perPage must be >= 1 but was " + perPage);
    }
  }

  /**
   * Returns the items on the given page.
   *
   * @param page the page to slice
   * @param perPage the page capacity; must be positive
   * @return the slice, or an empty list if the page is past the end
   */
  public List<MenuItem> page(PageNumber page, int perPage) {
    requirePositive(perPage);
    long from = (long) page.value() * perPage;
    if (from >= items.size()) {
      return List.of();
    }
    int start = (int) from;
    return items.subList(start, Math.min(start + perPage, items.size()));
  }

  /**
   * Tells whether a page after the given one holds any items.
   *
   * @param page the current page
   * @param perPage the page capacity; must be positive
   * @return {@code true} if a next page exists
   */
  public boolean hasNext(PageNumber page, int perPage) {
    requirePositive(perPage);
    return ((long) page.value() + 1) * perPage < items.size();
  }

  /**
   * Tells whether a page before the given one exists.
   *
   * @param page the current page
   * @return {@code true} if a previous page exists
   */
  public boolean hasPrevious(PageNumber page) {
    return page.value() > 0;
  }
}
