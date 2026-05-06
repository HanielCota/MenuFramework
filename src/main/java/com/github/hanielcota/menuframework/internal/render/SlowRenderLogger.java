package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

public final class SlowRenderLogger {

  private static final Logger log =
      Logger.getLogger(SlowRenderLogger.class.getName());

  @NonNull private final MenuFrameworkConfig config;

  public SlowRenderLogger(@NonNull MenuFrameworkConfig config) {
    this.config = config;
  }

  public void logIfSlow(@NonNull String menuId, long durationMillis) {
    long threshold = config.slowRenderThresholdMillis();
    if (threshold <= 0) return;
    if (config.logSlowRenders() && durationMillis > threshold) {
      log.log(
          Level.WARNING,
          () ->
              "Performance Smell: Slow dynamic content provider for menu '%s'. Duration: %dms (Threshold: %dms)"
                  .formatted(menuId, durationMillis, threshold));
    }
  }
}
