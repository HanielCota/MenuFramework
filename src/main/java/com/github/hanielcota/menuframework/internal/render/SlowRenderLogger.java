package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
@RequiredArgsConstructor
public final class SlowRenderLogger {

  @NonNull private final MenuFrameworkConfig config;

  public void logIfSlow(@NonNull String menuId, long durationMillis) {
    long threshold = config.slowRenderThresholdMillis();
    if (threshold <= 0) return;
    if (config.logSlowRenders() && durationMillis > threshold) {
      log.warn(
          "Performance Smell: Slow dynamic content provider for menu '{}'. Duration: {}ms (Threshold: {}ms)",
          menuId,
          durationMillis,
          threshold);
    }
  }
}
