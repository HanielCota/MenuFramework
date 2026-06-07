package dev.haniel.menu.paper.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.state.State;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.state.StateListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Adversarial anti-leak / notification-routing checks for {@link ReactiveBinding}: a bound state
 * change schedules exactly one coalesced flush, close both unbinds state and cancels a pending
 * flush, and a state mutated after close drives no further re-render.
 */
class ReactiveBindingNotificationEdgeCasesTest {

  private final ManualScheduler scheduler = new ManualScheduler();
  private final int[] flushes = {0};

  @Test
  void aBoundStateChangeSchedulesOneCoalescedFlush() {
    State<Integer> a = State.of(0);
    State<Integer> b = State.of(0);
    ReactiveBinding binding = bindingOver(List.of(a, b));
    StateListener view = binding::schedule;
    binding.bind(view);

    a.set(1);
    b.set(1);

    assertEquals(1, scheduler.pending(), "two changes in one window coalesce to one flush");
    scheduler.tick();
    assertEquals(1, flushes[0]);
  }

  @Test
  void closeUnbindsStateAndCancelsThePendingFlush() {
    State<Integer> a = State.of(0);
    ReactiveBinding binding = bindingOver(List.of(a));
    binding.bind(binding::schedule);
    a.set(1);
    assertEquals(1, scheduler.pending());

    binding.close();

    assertEquals(0, scheduler.pending(), "close must cancel the pending flush (anti-leak)");
  }

  @Test
  void stateMutatedAfterCloseDrivesNoReRender() {
    State<Integer> a = State.of(0);
    ReactiveBinding binding = bindingOver(List.of(a));
    binding.bind(binding::schedule);

    binding.close();
    a.set(42); // listener was unbound on close
    scheduler.tick();

    assertEquals(0, scheduler.pending(), "a closed view schedules nothing");
    assertEquals(0, flushes[0], "a closed view never re-renders");
  }

  private ReactiveBinding bindingOver(List<State<?>> states) {
    StateBinding binding = new StateBinding(states);
    Flusher flusher = new Flusher(scheduler, () -> flushes[0]++, logger());
    return new ReactiveBinding(binding, flusher);
  }

  private static Logger logger() {
    return Logger.getLogger(ReactiveBindingNotificationEdgeCasesTest.class.getName());
  }

  private static final class ManualScheduler implements PlayerScheduler {
    private final List<Runnable> queued = new ArrayList<>();

    @Override
    public ScheduledTask schedule(Runnable task) {
      queued.add(task);
      return () -> queued.remove(task);
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
}
