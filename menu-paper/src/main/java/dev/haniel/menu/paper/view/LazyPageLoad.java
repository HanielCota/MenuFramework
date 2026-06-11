package dev.haniel.menu.paper.view;

import dev.haniel.menu.compiler.binding.PageProvider;
import dev.haniel.menu.domain.Page;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.item.MenuItem;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Loads a lazily paginated page off-thread and delivers it back on the view's thread.
 *
 * <p>The blocking {@link PageProvider#load(int, int)} runs on the async executor; on success the
 * loaded page is handed to the consumer on the player's scheduler, where building Bukkit visuals is
 * safe. A failed load is logged and dropped, leaving the view on its current page.
 */
final class LazyPageLoad {

  private final PageProvider provider;
  private final int pageSize;
  private final LazyLoadContext context;

  /**
   * Wires a loader for one open lazy view.
   *
   * @param provider the bound page provider; never null
   * @param pageSize the number of content slots per page
   * @param context the async and view-thread executors; never null
   */
  LazyPageLoad(PageProvider provider, int pageSize, LazyLoadContext context) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.pageSize = pageSize;
    this.context = Objects.requireNonNull(context, "context");
  }

  /**
   * Loads the given page off-thread and delivers it to {@code onLoaded} on the view's thread.
   *
   * @param page the page to load
   * @param onLoaded the consumer run with the loaded page, on the view's thread
   */
  void fetch(PageNumber page, Consumer<Page<MenuItem>> onLoaded) {
    context.async().execute(() -> load(page, onLoaded));
  }

  private void load(PageNumber page, Consumer<Page<MenuItem>> onLoaded) {
    Page<MenuItem> loaded;
    try {
      loaded = provider.load(page.value(), pageSize);
    } catch (RuntimeException failure) {
      context.logger().log(Level.WARNING, "Lazy page load failed", failure);
      return;
    }
    context.viewThread().schedule(() -> onLoaded.accept(loaded));
  }
}
