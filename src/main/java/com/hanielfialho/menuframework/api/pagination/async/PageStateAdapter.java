package com.hanielfialho.menuframework.api.pagination.async;

import java.util.Objects;

/**
 * Adapter between a menu's complete state and one asynchronous page fragment.
 *
 * @param <S> complete menu-state type
 * @param <T> page-entry type
 */
public interface PageStateAdapter<S, T> {

  /**
   * Creates an identity adapter for menus whose complete state is an {@link AsyncPageState}.
   *
   * @param <T> entry type
   * @return identity adapter
   */
  static <T> PageStateAdapter<AsyncPageState<T>, T> identity() {
    return new PageStateAdapter<>() {
      /** {@inheritDoc} */
      @Override
      public AsyncPageState<T> pageState(AsyncPageState<T> state) {
        return Objects.requireNonNull(state, "state");
      }

      /** {@inheritDoc} */
      @Override
      public AsyncPageState<T> withPageState(AsyncPageState<T> state, AsyncPageState<T> pageState) {
        Objects.requireNonNull(state, "state");
        return Objects.requireNonNull(pageState, "pageState");
      }
    };
  }

  /**
   * Extracts the asynchronous page fragment.
   *
   * @param state non-null complete state
   * @return non-null page state
   */
  AsyncPageState<T> pageState(S state);

  /**
   * Produces a complete state containing a replacement page fragment.
   *
   * @param state current complete state
   * @param pageState replacement page fragment
   * @return non-null replacement complete state
   */
  S withPageState(S state, AsyncPageState<T> pageState);
}
