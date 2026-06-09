package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.domain.PlayerId;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * A per-player rate limiter: a click is accepted at most once per cooldown window.
 *
 * <p>Backed by an injectable clock so the window is testable without sleeping. {@link
 * #tryAcquire(PlayerId)} both checks and, when allowed, records the click atomically, so it is the
 * single gate a {@link CooldownGuard} consults and concurrent clicks for one player cannot both
 * slip through.
 *
 * <p>Entries for players whose window has fully elapsed are swept periodically, so the map stays
 * bounded by the number of players who clicked within the last window rather than growing for every
 * player who ever clicked.
 */
public final class Cooldown {

  private static final int SWEEP_INTERVAL = 256;

  private final long cooldownMillis;
  private final LongSupplier clock;
  private final Map<PlayerId, Long> lastRun = new ConcurrentHashMap<>();
  private final AtomicInteger writes = new AtomicInteger();

  /**
   * Creates a cooldown of the given length, reading time from the given clock.
   *
   * @param cooldownMillis the window length in milliseconds; must be {@code >= 1}
   * @param clock the millisecond time source; never null
   */
  public Cooldown(long cooldownMillis, LongSupplier clock) {
    if (cooldownMillis < 1) {
      throw new IllegalArgumentException("cooldownMillis must be >= 1 but was " + cooldownMillis);
    }
    this.cooldownMillis = cooldownMillis;
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Tries to accept a click for the player, recording it when the window has elapsed.
   *
   * @param player the clicking player; never null
   * @return {@code true} if the click is accepted, {@code false} if still cooling down
   */
  public boolean tryAcquire(PlayerId player) {
    long now = clock.getAsLong();
    boolean[] accepted = {false};
    lastRun.compute(
        player,
        (id, last) -> {
          if (last != null && now - last < cooldownMillis) {
            return last;
          }
          accepted[0] = true;
          return now;
        });
    sweepExpired(now);
    return accepted[0];
  }

  private void sweepExpired(long now) {
    if (writes.incrementAndGet() % SWEEP_INTERVAL != 0) {
      return;
    }
    lastRun.values().removeIf(last -> now - last >= cooldownMillis);
  }
}
