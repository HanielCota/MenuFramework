package com.hanielfialho.menuframework.api.pagination.async;

import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Immutable state machine for asynchronous pagination.
 *
 * <p>An instance is always in exactly one of {@link PageLoadStatus#LOADING}, {@link
 * PageLoadStatus#READY} or {@link PageLoadStatus#ERROR}. The generation identifies the active
 * request in the current session and prevents stale completions from replacing a more recent page.
 *
 * @param <T> entry type
 */
public final class AsyncPageState<T> {

  private final PageRequest request;
  private final PageLoadStatus status;
  private final long generation;
  private final @Nullable PageSlice<T> page;
  private final @Nullable PageLoadError error;

  private AsyncPageState(
      PageRequest request,
      PageLoadStatus status,
      long generation,
      @Nullable PageSlice<T> page,
      @Nullable PageLoadError error) {
    this.request = Objects.requireNonNull(request, "request");
    this.status = Objects.requireNonNull(status, "status");

    if (generation < 0L) {
      throw new IllegalArgumentException("generation must be >= 0: " + generation);
    }

    this.generation = generation;
    this.page = page;
    this.error = error;

    switch (status) {
      case LOADING -> {
        if (page != null || error != null) {
          throw new IllegalArgumentException("A loading state cannot contain a page or an error");
        }
      }

      case READY -> {
        Objects.requireNonNull(page, "page");

        if (error != null) {
          throw new IllegalArgumentException("A ready state cannot contain an error");
        }
      }

      case ERROR -> {
        Objects.requireNonNull(error, "error");

        if (page != null) {
          throw new IllegalArgumentException("An error state cannot contain a page");
        }
      }
    }
  }

  /**
   * Creates the initial loading state with generation zero.
   *
   * <p>The first paginator load replaces generation zero with the generation reserved by the
   * current session.
   *
   * @param request non-null initial request
   * @param <T> entry type
   * @return initial loading state
   */
  public static <T> AsyncPageState<T> initial(PageRequest request) {
    return new AsyncPageState<>(request, PageLoadStatus.LOADING, 0L, null, null);
  }

  /**
   * Returns the active page request.
   *
   * @return active request
   */
  public PageRequest request() {
    return this.request;
  }

  /**
   * Returns the cursor of the active request.
   *
   * @return active cursor
   */
  public PageCursor cursor() {
    return this.request.cursor();
  }

  /**
   * Returns the current load phase.
   *
   * @return load status
   */
  public PageLoadStatus status() {
    return this.status;
  }

  /**
   * Returns the generation associated with this load.
   *
   * @return zero before the first load starts, otherwise a positive value
   */
  public long generation() {
    return this.generation;
  }

  /**
   * Returns whether a request is in progress.
   *
   * @return {@code true} while loading
   */
  public boolean loading() {
    return this.status == PageLoadStatus.LOADING;
  }

  /**
   * Returns whether a validated page is available.
   *
   * @return {@code true} when the page is ready
   */
  public boolean ready() {
    return this.status == PageLoadStatus.READY;
  }

  /**
   * Returns whether the most recent load failed.
   *
   * @return {@code true} when this state represents a failure
   */
  public boolean failed() {
    return this.status == PageLoadStatus.ERROR;
  }

  /**
   * Returns the page when the state is ready.
   *
   * @return optional ready page
   */
  public Optional<PageSlice<T>> page() {
    return Optional.ofNullable(this.page);
  }

  /**
   * Returns the ready page.
   *
   * @return ready page
   * @throws IllegalStateException if the status is not READY
   */
  public PageSlice<T> requirePage() {
    if (this.page == null) {
      throw new IllegalStateException("The page is not available while status is " + this.status);
    }

    return this.page;
  }

  /**
   * Returns the load error when the state has failed.
   *
   * @return optional error
   */
  public Optional<PageLoadError> error() {
    return Optional.ofNullable(this.error);
  }

  /**
   * Returns the current load error.
   *
   * @return current error
   * @throws IllegalStateException if the status is not ERROR
   */
  public PageLoadError requireError() {
    if (this.error == null) {
      throw new IllegalStateException("The error is not available while status is " + this.status);
    }

    return this.error;
  }

  /**
   * Returns ready entries, or an empty list while loading or failed.
   *
   * @return immutable entry list
   */
  public List<T> entries() {
    return this.page == null ? List.of() : this.page.entries();
  }

  AsyncPageState<T> begin(PageRequest request, long generation) {
    Objects.requireNonNull(request, "request");

    if (generation <= 0L) {
      throw new IllegalArgumentException("generation must be greater than zero: " + generation);
    }

    /*
     * Generations belong to the session and MenuTaskKey, not to persisted
     * state. History may restore a READY state into a fresh session whose
     * first task starts again at generation one.
     */
    return new AsyncPageState<>(request, PageLoadStatus.LOADING, generation, null, null);
  }

  AsyncPageState<T> complete(long generation, PageSlice<T> page) {
    this.checkCompletionGeneration(generation);
    Objects.requireNonNull(page, "page");

    if (!page.request().equals(this.request)) {
      throw new IllegalArgumentException(
          "The loaded page does not match the active request: "
              + page.request()
              + " != "
              + this.request);
    }

    return new AsyncPageState<>(this.request, PageLoadStatus.READY, generation, page, null);
  }

  AsyncPageState<T> fail(long generation, PageLoadError error) {
    this.checkCompletionGeneration(generation);

    return new AsyncPageState<>(
        this.request,
        PageLoadStatus.ERROR,
        generation,
        null,
        Objects.requireNonNull(error, "error"));
  }

  private void checkCompletionGeneration(long generation) {
    if (this.status != PageLoadStatus.LOADING) {
      throw new IllegalStateException("Only a loading state can be completed: " + this.status);
    }

    if (generation != this.generation) {
      throw new IllegalArgumentException(
          "Generation does not match the active load: " + generation + " != " + this.generation);
    }
  }
}
