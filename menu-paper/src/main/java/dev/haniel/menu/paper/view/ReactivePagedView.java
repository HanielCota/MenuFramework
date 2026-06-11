package dev.haniel.menu.paper.view;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.compiler.binding.BoundTick;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.paper.holder.ClickableHolder;
import dev.haniel.menu.paper.holder.OpenMenu;
import dev.haniel.menu.paper.reactive.Flusher;
import dev.haniel.menu.paper.reactive.ReactiveBinding;
import dev.haniel.menu.paper.reactive.ReactiveLifecycle;
import dev.haniel.menu.paper.reactive.ReactiveView;
import dev.haniel.menu.paper.reactive.Ticking;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.paper.render.model.RenderedPage;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.state.StateListener;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * One player's open view of a reactive paginated menu.
 *
 * <p>After a successful open it binds the instance's states to itself. A state change bumps the
 * cache version and schedules a single coalesced flush; the flush re-renders the current page by
 * diff. Navigation re-renders immediately into the same inventory. Closing unbinds everything
 * (anti-leak).
 */
public final class ReactivePagedView
    implements ClickableHolder, ReactiveView, StateListener, OpenMenu {

  private final PageRenderer renderer;
  private final PageCursor cursor;
  private final ReactiveLifecycle lifecycle;
  private final LazyPageLoad lazy;
  private boolean closed;
  private long generation;

  /**
   * Builds a view. Binding is explicit so a failed initial render/open cannot leak this view.
   *
   * @param renderer the per-view renderer; never null
   * @param states the states discovered on the instance; never null
   * @param ticks the periodic ticks bound to the instance; never null
   * @param onClose the action to run when the view closes; never null
   * @param scheduler the owning player's scheduler, for re-renders and ticks; never null
   * @param lazy the off-thread page loader for a lazily paginated menu, or {@code null} when the
   *     content is an eager list rendered synchronously
   */
  public ReactivePagedView(
      PageRenderer renderer,
      StateBinding states,
      List<BoundTick> ticks,
      Runnable onClose,
      PlayerScheduler scheduler,
      Logger logger,
      LazyPageLoad lazy) {
    this.renderer = Objects.requireNonNull(renderer, "renderer");
    this.cursor = new PageCursor(renderer.newInventory(this));
    this.lazy = lazy;
    ReactiveBinding reactive =
        new ReactiveBinding(states, new Flusher(scheduler, this::flush, logger));
    this.lifecycle = new ReactiveLifecycle(reactive, new Ticking(scheduler, ticks), onClose);
  }

  /**
   * Builds an eager view whose content renders synchronously, with no lazy page loader.
   *
   * @param renderer the per-view renderer; never null
   * @param states the states discovered on the instance; never null
   * @param ticks the periodic ticks bound to the instance; never null
   * @param onClose the action to run when the view closes; never null
   * @param scheduler the owning player's scheduler, for re-renders and ticks; never null
   * @param logger the logger for coalesced-flush tracing; never null
   */
  public ReactivePagedView(
      PageRenderer renderer,
      StateBinding states,
      List<BoundTick> ticks,
      Runnable onClose,
      PlayerScheduler scheduler,
      Logger logger) {
    this(renderer, states, ticks, onClose, scheduler, logger, null);
  }

  /** Binds this opened view to its reactive states and starts its ticks. */
  public void bind() {
    lifecycle.bind(this);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return cursor.inventory();
  }

  /**
   * Shows the given page in this view's inventory.
   *
   * <p>Eager content renders and applies synchronously. Lazy content loads off-thread and applies
   * when it returns; meanwhile the current page stays put. Each call supersedes any in-flight load,
   * so only the most recently requested page is applied (rapid navigation, a refresh during a
   * load).
   *
   * @param page the page to show; clamped by the renderer
   */
  public void show(PageNumber page) {
    long requested = ++generation;
    if (lazy == null) {
      apply(requested, () -> renderer.render(page));
      return;
    }
    lazy.fetch(page, loaded -> apply(requested, () -> renderer.renderPage(page, loaded)));
  }

  private void apply(long requested, Supplier<RenderedPage> render) {
    if (closed || requested != generation) {
      return;
    }
    cursor.apply(render.get());
  }

  @Override
  public void onChange() {
    if (closed) {
      return;
    }
    renderer.invalidate();
    lifecycle.schedule();
  }

  @Override
  public MenuId menuId() {
    return renderer.menuId();
  }

  /** Re-renders on external request, exactly as a reactive state change would. */
  @Override
  public void refresh() {
    onChange();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    lifecycle.close();
    cursor.clear();
  }

  @Override
  public void click(int rawSlot, ClickContext context) {
    if (isNavigationSlot(rawSlot)) {
      navigate(rawSlot);
      return;
    }
    cursor.actionAt(rawSlot).ifPresent(action -> action.onClick(context));
  }

  private boolean isNavigationSlot(int rawSlot) {
    int previous = renderer.layout().previousSlot();
    int next = renderer.layout().nextSlot();
    return (previous >= 0 && rawSlot == previous) || (next >= 0 && rawSlot == next);
  }

  private void navigate(int rawSlot) {
    if (rawSlot == renderer.layout().previousSlot() && cursor.hasPrevious()) {
      show(cursor.page().previous());
      return;
    }
    if (rawSlot == renderer.layout().nextSlot() && cursor.hasNext()) {
      show(cursor.page().next());
    }
  }

  private void flush() {
    if (closed) {
      return;
    }
    show(cursor.page());
  }
}
