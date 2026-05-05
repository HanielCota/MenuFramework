package com.github.hanielcota.menuframework;

import com.github.hanielcota.menuframework.internal.config.ConfigValidator;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Configuration for the MenuFramework library.
 *
 * <p>All cache-related values must be positive. Use the setter methods to customize defaults.
 *
 * <p>Example:
 *
 * <pre>{@code
 * var config = new MenuFrameworkConfig()
 *     .sessionCacheMaxSize(1000)
 *     .pageCacheExpireMinutes(5);
 * }</pre>
 */
@Getter
@Accessors(fluent = true)
public final class MenuFrameworkConfig {

  private static final int DEFAULT_SESSION_CACHE_MAX_SIZE = 500;
  private static final int DEFAULT_SESSION_CACHE_EXPIRE_MINUTES = 5;
  private static final int DEFAULT_PAGE_CACHE_MAX_SIZE = 5_000;
  private static final int DEFAULT_PAGE_CACHE_EXPIRE_MINUTES = 10;
  private static final int DEFAULT_ITEMSTACK_CACHE_MAX_SIZE = 2_000;
  private static final int DEFAULT_ITEMSTACK_CACHE_EXPIRE_MINUTES = 30;
  private static final long DEFAULT_SLOW_RENDER_THRESHOLD_MS = 50;

  private int sessionCacheMaxSize = DEFAULT_SESSION_CACHE_MAX_SIZE;
  private int sessionCacheExpireMinutes = DEFAULT_SESSION_CACHE_EXPIRE_MINUTES;
  private int pageCacheMaxSize = DEFAULT_PAGE_CACHE_MAX_SIZE;
  private int pageCacheExpireMinutes = DEFAULT_PAGE_CACHE_EXPIRE_MINUTES;
  private int itemStackCacheMaxSize = DEFAULT_ITEMSTACK_CACHE_MAX_SIZE;
  private int itemStackCacheExpireMinutes = DEFAULT_ITEMSTACK_CACHE_EXPIRE_MINUTES;

  private boolean logSlowRenders = true;
  private long slowRenderThresholdMillis = DEFAULT_SLOW_RENDER_THRESHOLD_MS;

  public MenuFrameworkConfig sessionCacheMaxSize(int sessionCacheMaxSize) {
    this.sessionCacheMaxSize =
        ConfigValidator.requirePositive(sessionCacheMaxSize, "sessionCacheMaxSize");
    return this;
  }

  public MenuFrameworkConfig sessionCacheExpireMinutes(int sessionCacheExpireMinutes) {
    this.sessionCacheExpireMinutes =
        ConfigValidator.requirePositive(sessionCacheExpireMinutes, "sessionCacheExpireMinutes");
    return this;
  }

  public MenuFrameworkConfig pageCacheMaxSize(int pageCacheMaxSize) {
    this.pageCacheMaxSize = ConfigValidator.requirePositive(pageCacheMaxSize, "pageCacheMaxSize");
    return this;
  }

  public MenuFrameworkConfig pageCacheExpireMinutes(int pageCacheExpireMinutes) {
    this.pageCacheExpireMinutes =
        ConfigValidator.requirePositive(pageCacheExpireMinutes, "pageCacheExpireMinutes");
    return this;
  }

  public MenuFrameworkConfig itemStackCacheMaxSize(int itemStackCacheMaxSize) {
    this.itemStackCacheMaxSize =
        ConfigValidator.requirePositive(itemStackCacheMaxSize, "itemStackCacheMaxSize");
    return this;
  }

  public MenuFrameworkConfig itemStackCacheExpireMinutes(int itemStackCacheExpireMinutes) {
    this.itemStackCacheExpireMinutes =
        ConfigValidator.requirePositive(itemStackCacheExpireMinutes, "itemStackCacheExpireMinutes");
    return this;
  }

  public MenuFrameworkConfig logSlowRenders(boolean logSlowRenders) {
    this.logSlowRenders = logSlowRenders;
    return this;
  }

  public MenuFrameworkConfig slowRenderThresholdMillis(long slowRenderThresholdMillis) {
    this.slowRenderThresholdMillis =
        ConfigValidator.requireNonNegative(slowRenderThresholdMillis, "slowRenderThresholdMillis");
    return this;
  }
}
