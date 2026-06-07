package dev.haniel.menu.paper.view;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.paper.holder.ClickableHolder;
import dev.haniel.menu.paper.reactive.Flusher;
import dev.haniel.menu.paper.reactive.ReactiveBinding;
import dev.haniel.menu.paper.reactive.ReactiveView;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.state.StateListener;
import java.util.logging.Logger;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * One player's open view of a reactive paginated menu.
 *
 * <p>After a successful open it binds the instance's states to itself. A state change bumps the cache version and
 * schedules a single coalesced flush; the flush re-renders the current page by diff. Navigation
 * re-renders immediately into the same inventory. Closing unbinds everything (anti-leak).
 */
public final class ReactivePagedView implements ClickableHolder, ReactiveView, StateListener {

  private final PageRenderer renderer;
  private final PageCursor cursor;
  private final ReactiveBinding reactive;

  /**
   * Builds a view. Binding is explicit so a failed initial render/open cannot leak this view.
   *
   * @param renderer the per-view renderer; never null
   * @param states the states discovered on the instance; never null
   * @param scheduler the owning player's scheduler, for coalesced re-renders; never null
   */
  public ReactivePagedView(
      PageRenderer renderer, StateBinding states, PlayerScheduler scheduler, Logger logger) {
    this.renderer = renderer;
    this.cursor = new PageCursor(renderer.newInventory(this));
    this.reactive = new ReactiveBinding(states, new Flusher(scheduler, this::flush, logger));
  }

  /** Binds this opened view to its reactive states. */
  public void bind() {
    reactive.bind(this);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return cursor.inventory();
  }

  /**
   * Renders the given page into this view's inventory.
   *
   * @param page the page to show; clamped by the renderer
   */
  public void show(PageNumber page) {
    cursor.apply(renderer.render(page));
  }

  @Override
  public void onChange() {
    renderer.invalidate();
    reactive.schedule();
  }

  @Override
  public void close() {
    reactive.close();
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
    show(cursor.page());
  }
}
