package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.compiler.binding.BoundTick;
import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.InventoryFactory;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.model.Overlay;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.state.State;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.template.PagedContent;
import dev.haniel.menu.template.PagedDecor;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial interplay of {@link ReactivePagedView} with {@link PageCursor} and the real {@link
 * PageRenderer}, driven without a Bukkit server: the inventory is a Mockito mock so the diff writer
 * can write into it while rendering touches no live items.
 *
 * <p>Probes that paging routes through the cursor's live page, that a content click fires the
 * action of the CURRENTLY displayed page (never a stale slice), that boundary navigation is a
 * no-op, that a state change re-renders the same page through the scheduler, and that close unbinds
 * (and is safe before bind, as the open() recovery path relies on).
 *
 * <p>The page is never read directly — the view does not expose its cursor — so it is asserted
 * through observable behaviour: the per-item action records which content index it fired for.
 */
class ReactivePagedViewEdgeCasesTest {

  // Mask: previous '<' at 0, content 'X' at 1, next '>' at 2 (one content slot per page).
  private static final List<String> MASK = List.of("<X>      ");
  private static final int PREVIOUS_SLOT = 0;
  private static final int CONTENT_SLOT = 1;
  private static final int NEXT_SLOT = 2;

  @Test
  void clickOnContentRoutesToTheCurrentlyDisplayedPageNotAStalePage() {
    RecordingSource source = new RecordingSource(3); // one item per page across pages 0, 1, 2
    ReactivePagedView view = view(new ManualScheduler(), source);
    view.show(PageNumber.first());

    view.click(CONTENT_SLOT, click());
    assertEquals(0, source.lastClickedItem(), "first page content click must hit item 0");

    // Page forward, then click the SAME physical slot: it must route to the new page's item.
    view.click(NEXT_SLOT, click());
    view.click(CONTENT_SLOT, click());
    assertEquals(1, source.lastClickedItem(), "after paging, the same slot must hit the new page");

    view.click(NEXT_SLOT, click());
    view.click(CONTENT_SLOT, click());
    assertEquals(2, source.lastClickedItem(), "the third page must route to its own item");
  }

  @Test
  void nextAtTheLastPageIsANoOpAndKeepsActionsForThatPage() {
    RecordingSource source = new RecordingSource(2); // pages 0 and 1, one item each
    ReactivePagedView view = view(new ManualScheduler(), source);
    view.show(PageNumber.first());

    view.click(NEXT_SLOT, click()); // move to the last page
    view.click(NEXT_SLOT, click()); // next at the boundary must be a no-op, not a navigation route

    view.click(CONTENT_SLOT, click());
    assertEquals(
        1,
        source.lastClickedItem(),
        "the last page's action must stay bound after a boundary next");
  }

  @Test
  void previousAtTheFirstPageIsANoOp() {
    RecordingSource source = new RecordingSource(3);
    ReactivePagedView view = view(new ManualScheduler(), source);
    view.show(PageNumber.first());

    view.click(PREVIOUS_SLOT, click()); // no previous page exists yet

    view.click(CONTENT_SLOT, click());
    assertEquals(
        0, source.lastClickedItem(), "previous on the first page must not move the cursor");
  }

  @Test
  void stateChangeReRendersTheCurrentPageThroughTheScheduler() {
    ManualScheduler scheduler = new ManualScheduler();
    RecordingSource source = new RecordingSource(3);
    ReactivePagedView view = view(scheduler, source);
    view.show(PageNumber.first());
    view.click(NEXT_SLOT, click()); // now on page 1
    int providerCallsBefore = source.provideCount();

    view.onChange();
    assertEquals(
        1, scheduler.pending(), "a state change must schedule exactly one coalesced flush");

    scheduler.tick();
    assertTrue(
        source.provideCount() > providerCallsBefore,
        "the flush must re-run the provider for fresh actions");

    view.click(CONTENT_SLOT, click());
    assertEquals(
        1, source.lastClickedItem(), "the flush must re-render the same page, not reset to first");
  }

  @Test
  void closeUnbindsStatesAndCancelsPendingFlush() {
    ManualScheduler scheduler = new ManualScheduler();
    State<Integer> state = State.of(0);
    ReactivePagedView view = view(scheduler, new RecordingSource(1), List.of(state));
    view.show(PageNumber.first());
    view.bind();

    state.set(1); // notifies the bound view, which schedules a flush
    assertEquals(1, scheduler.pending(), "a bound state change schedules a flush");

    view.close();
    assertEquals(0, scheduler.pending(), "close must cancel the pending flush");

    state.set(2); // the view is unbound: no new flush may be scheduled
    assertEquals(0, scheduler.pending(), "a closed view reacts to nothing");
  }

  @Test
  void closeBeforeBindIsSafeForTheFailedOpenRecoveryPath() {
    ReactivePagedView view = view(new ManualScheduler(), new RecordingSource(1));
    view.show(PageNumber.first());

    // open() calls close() in its catch block before bind() may have run; it must not throw.
    view.close();
  }

  @Test
  void outOfBoundsContentClickIsASilentNoOp() {
    RecordingSource source = new RecordingSource(1);
    ReactivePagedView view = view(new ManualScheduler(), source);
    view.show(PageNumber.first());

    view.click(999, click());

    assertEquals(
        -1, source.lastClickedItem(), "a click past the inventory must route to no action");
  }

  @Test
  void closeRunsTheCloseHook() {
    ManualScheduler scheduler = new ManualScheduler();
    boolean[] closed = {false};
    ReactivePagedView view =
        new ReactivePagedView(
            renderer(new RecordingSource(1)),
            new StateBinding(List.of()),
            List.of(),
            () -> closed[0] = true,
            scheduler,
            logger());
    view.show(PageNumber.first());
    view.bind();

    view.close();

    assertTrue(closed[0], "close must run the close hook");
  }

  @Test
  void closeIsIdempotentAndRunsTheCloseHookOnce() {
    ManualScheduler scheduler = new ManualScheduler();
    int[] closes = {0};
    ReactivePagedView view =
        new ReactivePagedView(
            renderer(new RecordingSource(1)),
            new StateBinding(List.of()),
            List.of(),
            () -> closes[0]++,
            scheduler,
            logger());
    view.show(PageNumber.first());
    view.bind();

    view.close();
    view.close(); // a quit after a normal close must not run teardown twice

    assertEquals(1, closes[0], "a second close must be a no-op");
  }

  @Test
  void tickStartsOnBindAndCancelsOnClose() {
    ManualScheduler scheduler = new ManualScheduler();
    int[] runs = {0};
    BoundTick tick = new BoundTick(20, () -> runs[0]++);
    ReactivePagedView view = view(scheduler, new RecordingSource(1), List.of(), List.of(tick));
    view.show(PageNumber.first());

    view.bind();
    assertEquals(1, scheduler.repeatingCount(), "bind must start the periodic tick");

    scheduler.fireTicks();
    assertEquals(1, runs[0], "the tick must run on its schedule");

    view.close();
    assertEquals(0, scheduler.repeatingCount(), "close must cancel the tick (anti-leak)");
  }

  @Test
  void tickThatChangesStateDrivesAReRender() {
    ManualScheduler scheduler = new ManualScheduler();
    State<Integer> remaining = State.of(3);
    BoundTick countdown = new BoundTick(20, () -> remaining.set(remaining.get() - 1));
    RecordingSource source = new RecordingSource(1);
    ReactivePagedView view = view(scheduler, source, List.of(remaining), List.of(countdown));
    view.show(PageNumber.first());
    view.bind();
    int rendersBefore = source.provideCount();

    scheduler.fireTicks(); // tick -> state.set -> schedules one coalesced flush
    assertEquals(1, scheduler.pending(), "a tick that changes state schedules a re-render");

    scheduler.tick(); // run the coalesced flush
    assertTrue(source.provideCount() > rendersBefore, "the flush re-renders the page");
  }

  private static ClickContext click() {
    return mock(ClickContext.class);
  }

  private ReactivePagedView view(ManualScheduler scheduler, RecordingSource source) {
    return view(scheduler, source, List.of(), List.of());
  }

  private ReactivePagedView view(
      ManualScheduler scheduler, RecordingSource source, List<State<?>> states) {
    return view(scheduler, source, states, List.of());
  }

  private ReactivePagedView view(
      ManualScheduler scheduler,
      RecordingSource source,
      List<State<?>> states,
      List<BoundTick> ticks) {
    return new ReactivePagedView(
        renderer(source), new StateBinding(states), ticks, () -> {}, scheduler, logger());
  }

  private PageRenderer renderer(RecordingSource source) {
    PageScene scene =
        new PageScene(
            new MenuId("paged"),
            Component.text("Paged"),
            9,
            MaskLayout.resolve(MASK, 1),
            new PagedDecor<>(stack(Material.ARROW), stack(Material.SPECTRAL_ARROW), null),
            new PagedContent<>(provider(source), icon -> stack(Material.STONE)),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(scene, new PageCache(logger()), new DataVersion(), inventoryFactory());
  }

  private static InventoryFactory inventoryFactory() {
    return (holder, size, title) -> inventoryOfSize(size, holder);
  }

  private static Inventory inventoryOfSize(int size, InventoryHolder holder) {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(size);
    when(inventory.getHolder()).thenReturn(holder);
    return inventory;
  }

  private static ContentProvider provider(RecordingSource source) {
    try {
      return new ContentProvider(
          MethodHandles.lookup()
              .unreflect(RecordingSource.class.getDeclaredMethod("items"))
              .bindTo(source));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static ItemStack stack(Material material) {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> stack(material));
    when(item.getType()).thenReturn(material);
    return item;
  }

  private static Logger logger() {
    return Logger.getLogger(ReactivePagedViewEdgeCasesTest.class.getName());
  }

  /**
   * A content source producing one item per index, each carrying an action that records the index
   * it was fired for. {@code provideCount} exposes how many times the provider was queried.
   */
  public static final class RecordingSource {
    private final int count;
    private int lastClickedItem = -1;
    private int provideCount;

    public RecordingSource(int count) {
      this.count = count;
    }

    public List<MenuItem> items() {
      provideCount++;
      List<MenuItem> built = new ArrayList<>();
      for (int index = 0; index < count; index++) {
        int captured = index;
        built.add(
            MenuItem.of(Icon.of(Material.STONE.name()))
                .onClick(context -> lastClickedItem = captured));
      }
      return built;
    }

    int lastClickedItem() {
      return lastClickedItem;
    }

    int provideCount() {
      return provideCount;
    }
  }

  /**
   * A {@link PlayerScheduler} whose tick is driven by the test, mirroring the reactive cycle test.
   */
  private static final class ManualScheduler implements PlayerScheduler {
    private final List<Runnable> queued = new ArrayList<>();
    private final List<Runnable> repeating = new ArrayList<>();

    @Override
    public ScheduledTask schedule(Runnable task) {
      queued.add(task);
      return new ScheduledTask() {
        @Override
        public void cancel() {
          queued.remove(task);
        }

        @Override
        public boolean scheduled() {
          return true;
        }
      };
    }

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long period) {
      repeating.add(task);
      return () -> repeating.remove(task);
    }

    int pending() {
      return queued.size();
    }

    int repeatingCount() {
      return repeating.size();
    }

    void tick() {
      List<Runnable> due = List.copyOf(queued);
      queued.clear();
      due.forEach(Runnable::run);
    }

    void fireTicks() {
      List.copyOf(repeating).forEach(Runnable::run);
    }
  }
}
