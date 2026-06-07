package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Adversarial boundary and overflow probes for {@link Paginator}. */
class PaginatorEdgeCasesTest {

  private static List<MenuItem> items(int count) {
    return IntStream.rangeClosed(1, count)
        .mapToObj(index -> MenuItem.of(Icon.of("STONE")))
        .toList();
  }

  @Test
  void emptyPaginatorHasOnePage() {
    Paginator empty = new Paginator(List.of());
    assertEquals(1, empty.totalPages(9));
  }

  @Test
  void exactMultipleDoesNotAddPhantomPage() {
    // 40 items / 20 per page == exactly 2 pages, never 3.
    Paginator paginator = new Paginator(items(40));
    assertEquals(2, paginator.totalPages(20));
  }

  @Test
  void singleItemIsOnePage() {
    Paginator paginator = new Paginator(items(1));
    assertEquals(1, paginator.totalPages(20));
  }

  @Test
  void oneOverMultipleSpillsToNextPage() {
    Paginator paginator = new Paginator(items(21));
    assertEquals(2, paginator.totalPages(20));
  }

  @Test
  void perPageOfOneYieldsPagePerItem() {
    Paginator paginator = new Paginator(items(7));
    assertEquals(7, paginator.totalPages(1));
  }

  @Test
  void firstPageHasNoPreviousButHasNext() {
    Paginator paginator = new Paginator(items(40));
    assertFalse(paginator.hasPrevious(PageNumber.first()));
    assertTrue(paginator.hasNext(PageNumber.first(), 20));
  }

  @Test
  void lastFullExactPageHasNoNext() {
    // 40 items, 20 per page: page index 1 is the last; nothing follows.
    Paginator paginator = new Paginator(items(40));
    assertFalse(paginator.hasNext(new PageNumber(1), 20));
  }

  @Test
  void pageJustPastEndIsEmptyNotThrowing() {
    Paginator paginator = new Paginator(items(40));
    assertTrue(paginator.page(new PageNumber(2), 20).isEmpty());
  }

  @Test
  void exactBoundaryPageIsEmpty() {
    // 20 items, 20 per page: page 0 is full, page 1 (from == size) must be empty.
    Paginator paginator = new Paginator(items(20));
    assertTrue(paginator.page(new PageNumber(1), 20).isEmpty());
  }

  @Test
  void hasNextIsFalseOnEmptyPaginator() {
    Paginator empty = new Paginator(List.of());
    assertFalse(empty.hasNext(PageNumber.first(), 20));
  }

  // --- Overflow probes: a very large page index must not corrupt the slice math. ---

  @Test
  void hugePageIndexReturnsEmptyWithoutOverflow() {
    // from = page * perPage = (2^29) * 4 == 2^31, which overflows int to
    // Integer.MIN_VALUE (negative). A negative 'from' slips past the
    // (from >= size) guard and feeds a negative index to subList ->
    // IndexOutOfBoundsException. Correct behaviour: empty list (past the end).
    Paginator paginator = new Paginator(items(40));
    PageNumber farPast = new PageNumber(1 << 29);
    assertTrue(paginator.page(farPast, 4).isEmpty());
  }

  @Test
  void hasNextIsFalseForHugePageIndexWithoutOverflow() {
    // (page + 1) * perPage == (2^29 + 1) * 4 overflows int to a negative value,
    // which is < size, so production wrongly reports that another page exists.
    // Correct behaviour: no next page exists far past the end.
    Paginator paginator = new Paginator(items(40));
    PageNumber farPast = new PageNumber(1 << 29);
    assertFalse(paginator.hasNext(farPast, 4));
  }
}
