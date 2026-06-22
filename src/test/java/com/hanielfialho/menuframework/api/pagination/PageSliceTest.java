package com.hanielfialho.menuframework.api.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class PageSliceTest {

  @Test
  void knownTotalCalculatesNavigationAndPageCount() {
    PageSlice<String> page =
        PageSlice.knownTotal(new PageRequest(PageCursor.of(1), 3), List.of("d", "e", "f"), 8);

    assertEquals(2L, page.number());
    assertTrue(page.hasPrevious());
    assertTrue(page.hasNext());
    assertEquals(8L, page.totalElements().orElseThrow());
    assertEquals(3L, page.totalPages().orElseThrow());
  }

  @Test
  void emptyKnownCollectionStillHasOneVirtualPage() {
    PageSlice<String> page = PageSlice.knownTotal(PageRequest.first(5), List.of(), 0);

    assertTrue(page.isEmpty());
    assertFalse(page.hasPrevious());
    assertFalse(page.hasNext());
    assertEquals(1L, page.totalPages().orElseThrow());
  }

  @Test
  void unknownTotalKeepsOnlyExplicitHasNextInformation() {
    PageSlice<String> page = PageSlice.unknownTotal(PageRequest.first(2), List.of("a", "b"), true);

    assertTrue(page.hasNext());
    assertTrue(page.totalElements().isEmpty());
    assertTrue(page.totalPages().isEmpty());
  }

  @Test
  void entriesAreDefensivelyCopiedAndUnmodifiable() {
    List<String> source = new ArrayList<>(List.of("a", "b"));
    PageSlice<String> page = PageSlice.unknownTotal(PageRequest.first(2), source, false);

    source.set(0, "changed");

    assertEquals(List.of("a", "b"), page.entries());
    assertThrows(UnsupportedOperationException.class, () -> page.entries().add("c"));
  }

  @Test
  void rejectsMoreEntriesThanRequested() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PageSlice.unknownTotal(PageRequest.first(1), List.of("a", "b"), false));
  }

  @Test
  void rejectsKnownTotalSmallerThanCurrentEntries() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PageSlice.knownTotal(PageRequest.first(3), List.of("a", "b"), 1));
  }

  @Test
  void rejectsKnownTotalOffsetOutsideResultSet() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PageSlice.knownTotal(new PageRequest(PageCursor.of(2), 3), List.of(), 6));
  }

  @Test
  void rejectsIncompleteKnownTotalWindow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PageSlice.knownTotal(PageRequest.first(3), List.of("a", "b"), 8));
  }

  @Test
  void rejectsPartialUnknownTotalPageWithNextPage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PageSlice.unknownTotal(PageRequest.first(3), List.of("a", "b"), true));
  }
}
