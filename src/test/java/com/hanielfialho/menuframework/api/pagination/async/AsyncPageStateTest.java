package com.hanielfialho.menuframework.api.pagination.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import java.util.List;
import org.junit.jupiter.api.Test;

final class AsyncPageStateTest {

  @Test
  void initialStateIsLoadingAtGenerationZero() {
    PageRequest request = PageRequest.first(5);
    AsyncPageState<String> state = AsyncPageState.initial(request);

    assertEquals(request, state.request());
    assertEquals(PageLoadStatus.LOADING, state.status());
    assertEquals(0L, state.generation());
    assertTrue(state.loading());
    assertTrue(state.entries().isEmpty());
    assertThrows(IllegalStateException.class, state::requirePage);
  }

  @Test
  void beginUsesTheSessionTaskGeneration() {
    AsyncPageState<String> initial = AsyncPageState.initial(PageRequest.first(5));

    PageRequest secondRequest = new PageRequest(PageCursor.of(1), 5);

    AsyncPageState<String> loading = initial.begin(secondRequest, 1L);

    assertEquals(secondRequest, loading.request());
    assertEquals(1L, loading.generation());
    assertTrue(loading.loading());
    assertThrows(IllegalArgumentException.class, () -> loading.begin(secondRequest, 0L));
  }

  @Test
  void restoredReadyStateCanStartAtGenerationOneInANewSession() {
    PageRequest request = PageRequest.first(1);
    PageSlice<String> page = PageSlice.unknownTotal(request, List.of("value"), false);

    AsyncPageState<String> restored =
        AsyncPageState.<String>initial(request).begin(request, 7L).complete(7L, page);

    AsyncPageState<String> loading = restored.begin(request, 1L);

    assertTrue(loading.loading());
    assertEquals(1L, loading.generation());
  }

  @Test
  void matchingCompletionCreatesReadyState() {
    PageRequest request = PageRequest.first(2);
    AsyncPageState<String> loading = AsyncPageState.<String>initial(request).begin(request, 1L);

    PageSlice<String> page = PageSlice.unknownTotal(request, List.of("a", "b"), true);

    AsyncPageState<String> ready = loading.complete(1L, page);

    assertTrue(ready.ready());
    assertFalse(ready.loading());
    assertEquals(page, ready.requirePage());
    assertEquals(List.of("a", "b"), ready.entries());
  }

  @Test
  void completionRejectsStaleGenerationAndDifferentRequest() {
    PageRequest request = PageRequest.first(2);
    AsyncPageState<String> loading = AsyncPageState.<String>initial(request).begin(request, 3L);

    PageSlice<String> matchingPage = PageSlice.unknownTotal(request, List.of("a"), false);

    PageRequest differentRequest = new PageRequest(PageCursor.of(1), 2);

    PageSlice<String> differentPage = PageSlice.unknownTotal(differentRequest, List.of("b"), false);

    assertThrows(IllegalArgumentException.class, () -> loading.complete(2L, matchingPage));

    assertThrows(IllegalArgumentException.class, () -> loading.complete(3L, differentPage));
  }

  @Test
  void failureCreatesErrorStateForMatchingGeneration() {
    PageRequest request = PageRequest.first(2);
    AsyncPageState<String> loading = AsyncPageState.<String>initial(request).begin(request, 1L);

    PageLoadError error =
        new PageLoadError(IllegalStateException.class.getName(), "database unavailable");

    AsyncPageState<String> failed = loading.fail(1L, error);

    assertTrue(failed.failed());
    assertEquals(error, failed.requireError());
    assertTrue(failed.entries().isEmpty());
    assertThrows(IllegalStateException.class, failed::requirePage);
  }

  @Test
  void completedStateCannotBeCompletedAgain() {
    PageRequest request = PageRequest.first(1);
    PageSlice<String> page = PageSlice.unknownTotal(request, List.of("a"), false);

    AsyncPageState<String> ready =
        AsyncPageState.<String>initial(request).begin(request, 1L).complete(1L, page);

    assertThrows(IllegalStateException.class, () -> ready.complete(1L, page));
  }
}
