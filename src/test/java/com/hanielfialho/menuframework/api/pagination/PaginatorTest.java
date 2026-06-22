package com.hanielfialho.menuframework.api.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class PaginatorTest {

  @Test
  void snapshotsSourceCollection() {
    List<Integer> source = new ArrayList<>(List.of(1, 2, 3));
    Paginator<Integer> paginator = Paginator.copyOf(source);

    source.clear();

    assertEquals(3, paginator.totalElements());
    assertEquals(List.of(1, 2, 3), paginator.elements());
  }

  @Test
  void slicesCollectionUsingRequestCursorAndSize() {
    Paginator<Integer> paginator = Paginator.copyOf(List.of(0, 1, 2, 3, 4, 5, 6));

    PageSlice<Integer> page = paginator.page(new PageRequest(PageCursor.of(1), 3));

    assertEquals(List.of(3, 4, 5), page.entries());
    assertTrue(page.hasPrevious());
    assertTrue(page.hasNext());
    assertEquals(3L, page.totalPages().orElseThrow());
  }

  @Test
  void lastPageHasNoNextPage() {
    Paginator<Integer> paginator = Paginator.copyOf(List.of(0, 1, 2, 3, 4));

    PageSlice<Integer> page = paginator.page(new PageRequest(PageCursor.of(1), 3));

    assertEquals(List.of(3, 4), page.entries());
    assertFalse(page.hasNext());
  }

  @Test
  void cursorBeyondEndIsNormalizedToLastExistingPage() {
    Paginator<Integer> paginator = Paginator.copyOf(List.of(0, 1, 2, 3, 4));

    PageSlice<Integer> page = paginator.page(new PageRequest(PageCursor.of(99), 2));

    assertEquals(2, page.cursor().index());
    assertEquals(List.of(4), page.entries());
  }

  @Test
  void emptyPaginatorExposesOneEmptyPage() {
    Paginator<Integer> paginator = Paginator.copyOf(List.of());
    PageSlice<Integer> page = paginator.page(PageRequest.first(9));

    assertEquals(1, paginator.pageCount(9));
    assertEquals(PageCursor.FIRST, page.cursor());
    assertTrue(page.entries().isEmpty());
  }

  @Test
  void validatesPageSize() {
    Paginator<Integer> paginator = Paginator.copyOf(List.of(1));

    assertThrows(IllegalArgumentException.class, () -> paginator.pageCount(0));
  }
}
