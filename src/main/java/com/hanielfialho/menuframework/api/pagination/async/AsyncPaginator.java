package com.hanielfialho.menuframework.api.pagination.async;

import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import com.hanielfialho.menuframework.api.pagination.PaginationLayout;
import com.hanielfialho.menuframework.api.task.MenuAsyncActions;
import com.hanielfialho.menuframework.api.task.MenuTaskContext;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Reusable coordinator for asynchronous pagination.
 *
 * <p>Each paginator owns a {@link MenuTaskKey}. A new load using the same key invalidates the
 * previous session-owned operation. The runtime publishes a LOADING state, starts the source
 * outside the tick process, then returns to the viewer's entity scheduler and publishes READY or
 * ERROR only when the generation is still current.
 *
 * @param <T> entry type
 */
public final class AsyncPaginator<T> {

  private final MenuTaskKey taskKey;
  private final PageSource<T> source;

  private AsyncPaginator(MenuTaskKey taskKey, PageSource<T> source) {
    this.taskKey = Objects.requireNonNull(taskKey, "taskKey");
    this.source = Objects.requireNonNull(source, "source");
  }

  /**
   * Creates a paginator with an explicit task key.
   *
   * @param taskKey key unique to this logical operation inside a session
   * @param source non-null asynchronous source
   * @param <T> entry type
   * @return reusable paginator
   */
  public static <T> AsyncPaginator<T> create(MenuTaskKey taskKey, PageSource<T> source) {
    return new AsyncPaginator<>(taskKey, source);
  }

  /**
   * Creates a paginator from a textual task key.
   *
   * @param taskKey value accepted by {@link MenuTaskKey#of(String)}
   * @param source non-null asynchronous source
   * @param <T> entry type
   * @return reusable paginator
   */
  public static <T> AsyncPaginator<T> create(String taskKey, PageSource<T> source) {
    return create(MenuTaskKey.of(taskKey), source);
  }

  /**
   * Returns the key used for generation, replacement and cancellation.
   *
   * @return paginator task key
   */
  public MenuTaskKey taskKey() {
    return this.taskKey;
  }

  /**
   * Loads a page when the complete menu state is the page state.
   *
   * @param actions active asynchronous-action context
   * @param request requested page
   */
  public void load(MenuAsyncActions<AsyncPageState<T>> actions, PageRequest request) {
    this.load(actions, PageStateAdapter.identity(), request);
  }

  /**
   * Loads a page stored inside a composite menu state.
   *
   * @param actions active asynchronous-action context
   * @param adapter page-fragment adapter
   * @param request requested page
   * @param <S> complete menu-state type
   */
  public <S> void load(
      MenuAsyncActions<S> actions, PageStateAdapter<S, T> adapter, PageRequest request) {
    Objects.requireNonNull(actions, "actions");
    Objects.requireNonNull(adapter, "adapter");
    Objects.requireNonNull(request, "request");

    actions.executeAsync(
        this.taskKey,
        taskContext -> this.loadPage(taskContext, request),
        (currentState, generation) -> {
          AsyncPageState<T> currentPage =
              Objects.requireNonNull(
                  adapter.pageState(currentState), "The page state adapter returned null");
          AsyncPageState<T> loadingPage = currentPage.begin(request, generation);
          return Objects.requireNonNull(
              adapter.withPageState(currentState, loadingPage),
              "The page state adapter returned a null menu state");
        },
        (currentState, generation, page) -> {
          AsyncPageState<T> currentPage =
              Objects.requireNonNull(
                  adapter.pageState(currentState), "The page state adapter returned null");
          AsyncPageState<T> completedPage = currentPage.complete(generation, page);
          return Objects.requireNonNull(
              adapter.withPageState(currentState, completedPage),
              "The page state adapter returned a null menu state");
        },
        (currentState, generation, failure) -> {
          AsyncPageState<T> currentPage =
              Objects.requireNonNull(
                  adapter.pageState(currentState), "The page state adapter returned null");
          AsyncPageState<T> failedPage = currentPage.fail(generation, PageLoadError.from(failure));
          return Objects.requireNonNull(
              adapter.withPageState(currentState, failedPage),
              "The page state adapter returned a null menu state");
        });
  }

  /**
   * Loads a page using the size derived from a pagination layout.
   *
   * @param actions active asynchronous-action context
   * @param layout pagination layout
   * @param cursor requested cursor
   */
  public void load(
      MenuAsyncActions<AsyncPageState<T>> actions, PaginationLayout layout, PageCursor cursor) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(cursor, "cursor");
    this.load(actions, layout.request(cursor));
  }

  /**
   * Loads a composite-state page using a pagination layout.
   *
   * @param actions active asynchronous-action context
   * @param adapter page-fragment adapter
   * @param layout pagination layout
   * @param cursor requested cursor
   * @param <S> complete menu-state type
   */
  public <S> void load(
      MenuAsyncActions<S> actions,
      PageStateAdapter<S, T> adapter,
      PaginationLayout layout,
      PageCursor cursor) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(cursor, "cursor");
    this.load(actions, adapter, layout.request(cursor));
  }

  /**
   * Reloads the current request when the complete menu state is paginated.
   *
   * @param actions active asynchronous-action context
   */
  public void reload(MenuAsyncActions<AsyncPageState<T>> actions) {
    Objects.requireNonNull(actions, "actions");
    this.load(actions, actions.state().request());
  }

  /**
   * Reloads the current request stored inside a composite state.
   *
   * @param actions active asynchronous-action context
   * @param adapter page-fragment adapter
   * @param <S> complete menu-state type
   */
  public <S> void reload(MenuAsyncActions<S> actions, PageStateAdapter<S, T> adapter) {
    Objects.requireNonNull(actions, "actions");
    Objects.requireNonNull(adapter, "adapter");

    AsyncPageState<T> pageState =
        Objects.requireNonNull(
            adapter.pageState(actions.state()), "The page state adapter returned null");

    this.load(actions, adapter, pageState.request());
  }

  private CompletionStage<PageSlice<T>> loadPage(MenuTaskContext taskContext, PageRequest request)
      throws Exception {
    PageLoadContext pageContext =
        new PageLoadContext(
            taskContext.sessionId(),
            taskContext.viewerId(),
            taskContext.key(),
            taskContext.generation());

    CompletionStage<PageSlice<T>> stage =
        Objects.requireNonNull(
            this.source.load(pageContext, request), "The page source returned null");

    return stage.thenApply(
        page -> {
          Objects.requireNonNull(page, "The page source completed with null");

          if (!page.request().equals(request)) {
            throw new IllegalArgumentException(
                "The page source returned a result for another request: "
                    + page.request()
                    + " != "
                    + request);
          }

          return page;
        });
  }
}
