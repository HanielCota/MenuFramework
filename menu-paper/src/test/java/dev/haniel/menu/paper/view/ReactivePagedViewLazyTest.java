package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.compiler.binding.PageProvider;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.Page;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.model.Overlay;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.PagedContent;
import dev.haniel.menu.template.PagedDecor;
import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Async-lifecycle probes for a lazily paginated {@link ReactivePagedView}, driven without a server:
 * the off-thread load and the view-thread apply are queues the test drains by hand.
 *
 * <p>A {@code show} loads off-thread and applies on the view thread; a superseded load is dropped
 * so only the latest page renders; a load that returns after the view closes is dropped; a failed
 * load leaves the view on its current page.
 */
class ReactivePagedViewLazyTest {

  private static final List<String> MASK = List.of("<XX>     ");

  @Test
  void showLoadsOffThreadThenAppliesOnTheViewThread() {
    Harness harness = new Harness();

    harness.view.show(PageNumber.first());

    assertEquals(1, harness.async.size(), "show must queue an off-thread load");
    assertEquals(0, harness.renders.get(), "nothing renders before the load returns");
    harness.async.runAll();
    assertEquals(1, harness.viewThread.size(), "a finished load queues a view-thread apply");
    harness.viewThread.runAll();
    assertEquals(1, harness.renders.get(), "the applied page renders its one item");
  }

  @Test
  void loadRunsWithThePageNumberAndContentSlotCount() {
    Harness harness = new Harness();

    harness.view.show(new PageNumber(2));
    harness.async.runAll();

    assertArrayEquals(
        new int[] {2, 2},
        harness.pages.calls.getFirst(),
        "the provider must load the requested page with the content-slot count as the page size");
  }

  @Test
  void aSupersededLoadIsDroppedSoOnlyTheLatestPageRenders() {
    Harness harness = new Harness();

    harness.view.show(PageNumber.first());
    harness.view.show(new PageNumber(1));
    harness.async.runAll(); // both loads run and queue their applies
    harness.viewThread
        .runAll(); // both applies run; the stale one is dropped by the generation guard

    assertEquals(2, harness.pages.calls.size(), "both pages were loaded");
    assertEquals(1, harness.renders.get(), "only the most recently requested page renders");
  }

  @Test
  void aLoadReturningAfterCloseIsDropped() {
    Harness harness = new Harness();
    harness.view.show(PageNumber.first());
    harness.async.runAll(); // load finished, apply queued

    harness.view.close();
    harness.viewThread.runAll(); // the apply runs after close

    assertEquals(0, harness.renders.get(), "an apply after close must not render");
  }

  @Test
  void aFailedLoadKeepsTheCurrentPage() {
    Harness harness = new Harness();
    harness.pages.fail = true;

    harness.view.show(PageNumber.first());
    harness.async.runAll(); // load throws, is logged and dropped

    assertEquals(0, harness.viewThread.size(), "a failed load must not queue an apply");
    assertEquals(0, harness.renders.get(), "a failed load must not render");
    assertFalse(harness.pages.calls.isEmpty(), "the provider was still invoked");
  }

  /** One open lazy view with hand-drainable async and view-thread queues. */
  private static final class Harness {
    private final TaskQueue async = new TaskQueue();
    private final TaskQueue viewThread = new TaskQueue();
    private final RecordingPages pages = new RecordingPages();
    private final AtomicInteger renders = new AtomicInteger();
    private final ReactivePagedView view = build();

    private ReactivePagedView build() {
      PageRenderer renderer = renderer(renders);
      LazyPageLoad lazy =
          new LazyPageLoad(
              provider(pages), 2, new LazyLoadContext(async, queueScheduler(viewThread), logger()));
      return new ReactivePagedView(
          renderer,
          new StateBinding(List.of()),
          List.of(),
          () -> {},
          queueScheduler(async),
          logger(),
          lazy);
    }
  }

  private static PageRenderer renderer(AtomicInteger renders) {
    IconFactory<ItemStack> icons =
        icon -> {
          renders.incrementAndGet();
          return stack();
        };
    PageScene scene =
        new PageScene(
            new MenuId("lazy"),
            Component.text("Lazy"),
            9,
            MaskLayout.resolve(MASK, 1),
            new PagedDecor<>(stack(), stack(), null),
            new PagedContent<>(ContentProvider.empty(), icons),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(scene, new PageCache(logger()), new DataVersion(), inventories());
  }

  private static PageProvider provider(RecordingPages pages) {
    try {
      java.lang.reflect.Method load =
          RecordingPages.class.getDeclaredMethod("load", int.class, int.class);
      load.setAccessible(true);
      return new PageProvider(MethodHandles.lookup().unreflect(load).bindTo(pages));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static PlayerScheduler queueScheduler(TaskQueue queue) {
    return new PlayerScheduler() {
      @Override
      public ScheduledTask schedule(Runnable task) {
        queue.execute(task);
        return noOpTask();
      }

      @Override
      public ScheduledTask scheduleRepeating(Runnable task, long period) {
        return noOpTask();
      }
    };
  }

  private static dev.haniel.menu.paper.render.InventoryFactory inventories() {
    return (holder, size, title) -> {
      Inventory inventory = mock(Inventory.class);
      when(inventory.getSize()).thenReturn(size);
      when(inventory.getHolder()).thenReturn(holder);
      return inventory;
    };
  }

  private static ItemStack stack() {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> stack());
    return item;
  }

  private static Logger logger() {
    return Logger.getLogger(ReactivePagedViewLazyTest.class.getName());
  }

  private static ScheduledTask noOpTask() {
    return new ScheduledTask() {
      @Override
      public void cancel() {}

      @Override
      public boolean scheduled() {
        return true;
      }
    };
  }

  /** An executor that holds tasks until the test drains them. */
  private static final class TaskQueue implements Executor {
    private final Deque<Runnable> tasks = new ArrayDeque<>();

    @Override
    public void execute(Runnable task) {
      tasks.add(task);
    }

    int size() {
      return tasks.size();
    }

    void runAll() {
      while (!tasks.isEmpty()) {
        tasks.removeFirst().run();
      }
    }
  }

  /** Records each load and returns a single-item page, or throws when {@code fail} is set. */
  private static final class RecordingPages {
    private final List<int[]> calls = new ArrayList<>();
    private boolean fail;

    @SuppressWarnings("unused") // bound reflectively as the page provider
    private Page<MenuItem> load(int page, int pageSize) {
      calls.add(new int[] {page, pageSize});
      if (fail) {
        throw new IllegalStateException("boom");
      }
      return Page.of(List.of(MenuItem.of(Icon.of("STONE"))), true);
    }
  }
}
