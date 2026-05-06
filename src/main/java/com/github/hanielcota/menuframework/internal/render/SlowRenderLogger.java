package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import org.jspecify.annotations.NonNull;

public final class SlowRenderLogger {

  private static final java.util.logging.Logger log =
      java.util.logging.Logger.getLogger(SlowRenderLogger.class.getName());

  @NonNull private final MenuFrameworkConfig config;

  public SlowRenderLogger(@NonNull MenuFrameworkConfig config) {
    this.config = config;
  }

  public void logIfSlow(@NonNull String menuId, long durationMillis) {
    long threshold = config.slowRenderThresholdMillis();
    if (threshold <= 0) return;
    if (config.logSlowRenders() && durationMillis > threshold) {
      log.log(
          java.util.logging.Level.WARNING,
          "Performance Smell: Slow dynamic content provider for menu '%s'. Duration: %dms (Threshold: %dms)"
              .formatted(menuId, durationMillis, threshold));
    }
  }
}
