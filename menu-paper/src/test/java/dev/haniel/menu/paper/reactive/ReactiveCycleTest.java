package dev.haniel.menu.paper.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.state.State;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.state.StateListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test of the reactive re-render cycle, wired from the same parts production uses but
 * without a Bukkit server: a real {@link PagedReader} reads {@code @Reactive}/{@code @Button} off
 * an annotated class, the states are bound through {@link ReactiveBinding}/{@link Flusher} to a
 * controllable scheduler, and clicking a button drives the coalesced re-render. The ItemStack and
 * inventory rendering is the only server-bound part and is deliberately left out (see the
 * class-test notes).
 */
class ReactiveCycleTest {

  private final ManualScheduler scheduler = new ManualScheduler();
  private final RenderRecorder render = new RenderRecorder();
  private ReactiveSample menu;
  private ReactiveBinding reactive;
  private MenuAction bump;

  @BeforeEach
  void openView() {
    PagedStructure structure = new PagedReader().read(ReactiveSample.class);
    menu = (ReactiveSample) structure.instantiator().create();
    StateBinding states = new StateBinding(readStates(structure, menu));
    Flusher flusher = new Flusher(scheduler, () -> render.capture(menu.clicks()), logger());
    reactive = new ReactiveBinding(states, flusher);
    StateListener view = reactive::schedule;
    reactive.bind(view);
    bump = structure.buttons().get(new ButtonId("bump")).bind(menu);
  }

  @Test
  void coalescesManyChangesInOneClickIntoASingleRender() {
    bump.onClick(null); // the button changes two states in one click
    assertEquals(0, render.count(), "must not render before the tick");
    assertEquals(1, scheduler.pending(), "the two state changes coalesce into one scheduled flush");

    scheduler.tick();

    assertEquals(1, render.count(), "exactly one re-render for the click");
    assertEquals(1, render.lastClicks(), "the render sees the latest coalesced state");
  }

  @Test
  void rendersOncePerTickAcrossClicks() {
    bump.onClick(null);
    scheduler.tick();
    bump.onClick(null);
    scheduler.tick();

    assertEquals(2, render.count());
    assertEquals(2, render.lastClicks());
  }

  @Test
  void closeCancelsPendingRenderAndStopsReactingToState() {
    bump.onClick(null);
    assertEquals(1, scheduler.pending());

    reactive.close();
    assertEquals(0, scheduler.pending(), "close cancels the pending flush");

    bump.onClick(null); // state still mutates, but the view is unbound
    scheduler.tick();
    assertEquals(0, scheduler.pending(), "a closed view schedules nothing");
    assertEquals(0, render.count(), "a closed view never re-renders");
  }

  @Test
  void rejectedScheduleDoesNotLeaveFlushPending() {
    RejectingScheduler rejecting = new RejectingScheduler();
    Flusher flusher = new Flusher(rejecting, () -> render.capture(1), logger());

    flusher.mark();
    flusher.mark();

    assertEquals(2, rejecting.attempts(), "a rejected schedule must not stay pending forever");
    assertEquals(0, render.count());
  }

  private static List<State<?>> readStates(PagedStructure structure, Object instance) {
    return structure.states().stream().<State<?>>map(field -> field.read(instance)).toList();
  }

  private static Logger logger() {
    return Logger.getLogger(ReactiveCycleTest.class.getName());
  }

  /** Records re-renders the way {@code ReactivePagedView.flush} would, capturing the state seen. */
  private static final class RenderRecorder {
    private int count;
    private int lastClicks = -1;

    void capture(int clicks) {
      count++;
      lastClicks = clicks;
    }

    int count() {
      return count;
    }

    int lastClicks() {
      return lastClicks;
    }
  }

  /** A {@link PlayerScheduler} whose "tick" is driven by the test instead of the server. */
  private static final class ManualScheduler implements PlayerScheduler {
    private final List<Runnable> queued = new ArrayList<>();

    @Override
    public ScheduledTask schedule(Runnable task) {
      queued.add(task);
      return () -> queued.remove(task);
    }

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long period) {
      return () -> {};
    }

    int pending() {
      return queued.size();
    }

    void tick() {
      List<Runnable> due = List.copyOf(queued);
      queued.clear();
      due.forEach(Runnable::run);
    }
  }

  private static final class RejectingScheduler implements PlayerScheduler {
    private int attempts;

    @Override
    public ScheduledTask schedule(Runnable task) {
      attempts++;
      return new ScheduledTask() {
        @Override
        public void cancel() {}

        @Override
        public boolean scheduled() {
          return false;
        }
      };
    }

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long period) {
      return schedule(task);
    }

    int attempts() {
      return attempts;
    }
  }

  @Menu(id = "reactive-sample")
  public static final class ReactiveSample {
    @Reactive private final State<Integer> clicks = State.of(0);
    @Reactive private final State<String> label = State.of("a");

    @Button(id = "bump")
    public void bump() {
      clicks.set(clicks.get() + 1);
      label.set(label.get() + "x");
    }

    int clicks() {
      return clicks.get();
    }

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }
}
