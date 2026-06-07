package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class PaginatorTest {

  private final Paginator paginator = new Paginator(items(50));

  @Test
  void computesTotalPages() {
    assertEquals(3, paginator.totalPages(20));
  }

  @Test
  void slicesMiddlePageFully() {
    assertEquals(20, paginator.page(new PageNumber(1), 20).size());
  }

  @Test
  void slicesLastPagePartially() {
    assertEquals(10, paginator.page(new PageNumber(2), 20).size());
  }

  @Test
  void returnsEmptyPastTheEnd() {
    assertTrue(paginator.page(new PageNumber(5), 20).isEmpty());
  }

  @Test
  void reportsNavigationBounds() {
    assertFalse(paginator.hasPrevious(PageNumber.first()));
    assertTrue(paginator.hasNext(PageNumber.first(), 20));
    assertFalse(paginator.hasNext(new PageNumber(2), 20));
  }

  private static List<MenuItem> items(int count) {
    return IntStream.rangeClosed(1, count)
        .mapToObj(index -> MenuItem.of(Icon.of("STONE")))
        .toList();
  }
}
