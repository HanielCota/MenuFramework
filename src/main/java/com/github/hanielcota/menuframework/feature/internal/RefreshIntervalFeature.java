package com.github.hanielcota.menuframework.feature.internal;

import com.github.hanielcota.menuframework.api.RefreshingMenuFeature;

/**
 * Refreshes the menu at a fixed interval. Useful for live counters, clocks, etc.
 *
 * <p>The task is automatically canceled when the menu closes.
 */
public record RefreshIntervalFeature(long refreshIntervalTicks) implements RefreshingMenuFeature {

  public RefreshIntervalFeature {
    if (refreshIntervalTicks <= 0) {
      throw new IllegalArgumentException(
          "refresh interval must be > 0, got: " + refreshIntervalTicks);
    }
  }
}
