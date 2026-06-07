package dev.haniel.menu.paper.render.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A monotonically increasing version of a menu's source data.
 *
 * <p>Part of the page cache key: bumping it makes every cached page for the menu miss on the next
 * render, so the visuals are rebuilt from the changed data. Click actions never come from the
 * cache, so they are always current regardless of the version.
 */
public final class DataVersion {

  private final AtomicLong value = new AtomicLong();

  /**
   * Returns the current version.
   *
   * @return the version value
   */
  public long current() {
    return value.get();
  }

  /** Advances the version, invalidating cached pages on the next render. */
  public void bump() {
    value.incrementAndGet();
  }
}
