package com.github.hanielcota.menuframework.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.internal.session.MenuSessionImpl;
import com.github.hanielcota.menuframework.pagination.PageCacheKey;
import com.github.hanielcota.menuframework.pagination.PageView;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public final class MenuCacheFactory {

  private MenuCacheFactory() {}

  public static @NonNull Cache<UUID, MenuSessionImpl> createSessionCache(
      @NonNull MenuFrameworkConfig configuration,
      @NonNull RemovalListener<UUID, MenuSessionImpl> removalListener) {

    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(removalListener, "removalListener");

    int maxSize = configuration.sessionCacheMaxSize();
    if (maxSize <= 0) {
      throw new IllegalArgumentException("sessionCacheMaxSize must be > 0, got: " + maxSize);
    }
    int expireMinutes = configuration.sessionCacheExpireMinutes();
    if (expireMinutes <= 0) {
      throw new IllegalArgumentException(
          "sessionCacheExpireMinutes must be > 0, got: " + expireMinutes);
    }

    return Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
        .recordStats()
        .removalListener(removalListener)
        .build();
  }

  public static @NonNull Cache<PageCacheKey, PageView> createPageCache(
      @NonNull MenuFrameworkConfig configuration) {
    Objects.requireNonNull(configuration, "configuration");
    int maxSize = configuration.pageCacheMaxSize();
    if (maxSize <= 0) {
      throw new IllegalArgumentException("pageCacheMaxSize must be > 0, got: " + maxSize);
    }
    int expireMinutes = configuration.pageCacheExpireMinutes();
    if (expireMinutes <= 0) {
      throw new IllegalArgumentException(
          "pageCacheExpireMinutes must be > 0, got: " + expireMinutes);
    }
    return Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
        .recordStats()
        .build();
  }

  public static @NonNull Cache<ItemTemplate, ItemStack> createItemStackCache(@NonNull MenuFrameworkConfig configuration) {
    Objects.requireNonNull(configuration, "configuration");
    int maxSize = configuration.itemStackCacheMaxSize();
    if (maxSize <= 0) {
      throw new IllegalArgumentException("itemStackCacheMaxSize must be > 0, got: " + maxSize);
    }
    int expireMinutes = configuration.itemStackCacheExpireMinutes();
    if (expireMinutes <= 0) {
      throw new IllegalArgumentException(
          "itemStackCacheExpireMinutes must be > 0, got: " + expireMinutes);
    }
    return Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(expireMinutes, TimeUnit.MINUTES)
        .build();
  }
}
