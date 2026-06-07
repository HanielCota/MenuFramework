package dev.haniel.menu.paper.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Adversarial coalescing/notification semantics for {@link Flusher}: exactly one flush per pending
 * window, a fresh window after a run, cancel-after-run being a no-op, re-marking from inside a
 * flush scheduling the next tick, and a rejected schedule never sticking.
 */
class FlusherNotificationEdgeCasesTest {

  private final ManualScheduler scheduler = new ManualScheduler();
  private final int[] flushes = {0};
  private final Flusher flusher = new Flusher(scheduler, () -> flushes[0]++, logger());

  @Test
  void manyMarksBeforeATickCoalesceIntoOneFlush() {
    flusher.mark();
    flusher.mark();
    flusher.mark();

    assertEquals(1, scheduler.pending(), "marks while pending must not schedule again");

    scheduler.tick();

    assertEquals(1, flushes[0], "exactly one flush for the coalesced window");
  }

  @Test
  void aNewMarkAfterAFlushOpensAFreshWindow() {
    flusher.mark();
    scheduler.tick();
    assertEquals(1, flushes[0]);

    flusher.mark();
    assertEquals(1, scheduler.pending(), "after running, the flusher must accept a new schedule");
    scheduler.tick();

    assertEquals(2, flushes[0]);
  }

  @Test
  void cancelAfterRunIsANoOpAndDoesNotCancelTheNextWindow() {
    flusher.mark();
    scheduler.tick(); // task ran, internal handle cleared
    flusher.cancel(); // must be a harmless no-op, not crash or poison state

    flusher.mark();
    scheduler.tick();

    assertEquals(2, flushes[0], "cancel after a completed run must not block future flushes");
  }

  @Test
  void remarkFromInsideAFlushSchedulesExactlyOneFollowUp() {
    int[] reentrantFlushes = {0};
    Flusher reentrant =
        new Flusher(
            scheduler,
            new Runnable() {
              @Override
              public void run() {
                reentrantFlushes[0]++;
                if (reentrantFlushes[0] == 1) {
                  // simulate a state change happening during the re-render
                  flusherRef[0].mark();
                }
              }
            },
            logger());
    flusherRef[0] = reentrant;

    reentrant.mark();
    scheduler.tick(); // runs flush #1, which re-marks
    assertEquals(1, scheduler.pending(), "re-mark during flush must schedule the next window");
    scheduler.tick(); // runs flush #2, no further re-mark

    assertEquals(2, reentrantFlushes[0]);
    assertEquals(0, scheduler.pending(), "no runaway scheduling");
  }

  @Test
  void rejectedScheduleNeverLeavesAPendingTaskBlockingFutureMarks() {
    RejectingScheduler rejecting = new RejectingScheduler();
    Flusher viaRejecting = new Flusher(rejecting, () -> flushes[0]++, logger());

    viaRejecting.mark();
    viaRejecting.mark();

    assertEquals(2, rejecting.attempts(), "a rejected schedule must reset, allowing a retry");
    assertFalse(rejecting.cancelled(), "a never-accepted task must not be cancelled");
  }

  private final Flusher[] flusherRef = new Flusher[1];

  private static Logger logger() {
    return Logger.getLogger(FlusherNotificationEdgeCasesTest.class.getName());
  }

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
    private boolean cancelled;

    @Override
    public ScheduledTask schedule(Runnable task) {
      attempts++;
      return new ScheduledTask() {
        @Override
        public void cancel() {
          cancelled = true;
        }

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

    boolean cancelled() {
      return cancelled;
    }
  }
}
