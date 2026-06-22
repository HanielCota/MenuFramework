package com.hanielfialho.menuframework.api.pagination.async;

import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import java.util.concurrent.CompletionStage;

/**
 * Asynchronous source of page data.
 *
 * <p>The operation is started from Paper's asynchronous scheduler. An implementation must not
 * access region-sensitive Bukkit objects such as players, worlds, inventories or item stacks.
 * Return immutable domain data that is safe to transfer between threads.
 *
 * @param <T> entry type
 */
@FunctionalInterface
public interface PageSource<T> {

  /**
   * Starts loading a page.
   *
   * @param context immutable request metadata
   * @param request requested page window
   * @return non-null stage that completes with a non-null page
   * @throws Exception if the operation cannot be created
   */
  CompletionStage<PageSlice<T>> load(PageLoadContext context, PageRequest request) throws Exception;
}
