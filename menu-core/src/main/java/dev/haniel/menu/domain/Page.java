package dev.haniel.menu.domain;

import java.util.List;
import java.util.Objects;

/**
 * One page of lazily loaded content, returned by a paginated menu's page provider.
 *
 * <p>Carries the items for a single page plus whether a page after it exists, so the framework can
 * enable the next-page button without knowing the total size. The provider is called per page, on
 * demand, which fits a real data source (a database cursor, a paged API).
 *
 * @param items the items on this page; never null, never larger than the page size requested
 * @param hasNext whether a page after this one exists
 * @param <T> the item type, typically {@code MenuItem}
 */
public record Page<T>(List<T> items, boolean hasNext) {

  public Page {
    items = List.copyOf(Objects.requireNonNull(items, "items"));
  }

  /**
   * Creates a page from its items and whether more follow.
   *
   * @param items the items on this page; never null
   * @param hasNext whether a page after this one exists
   * @param <T> the item type
   * @return the page
   */
  public static <T> Page<T> of(List<T> items, boolean hasNext) {
    return new Page<>(items, hasNext);
  }
}
