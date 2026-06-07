package dev.haniel.menu.paper.render.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

/**
 * A short-lived cache of rendered pages, keyed by {@link PageKey}.
 *
 * <p>Caches presentation only — the {@link ItemStack} array for a page's content slots — never
 * actions. Building those items (material lookup, MiniMessage parsing, item meta) is the costly
 * part skipped on a hit. Hits and misses are logged at {@code FINE} for debugging.
 */
public final class PageCache {

  private final Cache<PageKey, ItemStack[]> cache;
  private final Logger logger;

  /**
   * Creates a cache bounded by size and write age.
   *
   * @param logger the logger for hit/miss tracing; never null
   */
  public PageCache(Logger logger) {
    this.cache =
        Caffeine.newBuilder().maximumSize(64).expireAfterWrite(Duration.ofMinutes(2)).build();
    this.logger = logger;
  }

  /**
   * Returns the cached visuals for the key, building and storing them on a miss.
   *
   * @param key the page key
   * @param loader builds the visuals when absent; never null
   * @return the cached or freshly built visuals
   */
  public ItemStack[] get(PageKey key, Supplier<ItemStack[]> loader) {
    ItemStack[] cached = cache.getIfPresent(key);
    if (cached != null) {
      logger.fine(() -> "page cache hit " + key);
      return copy(cached);
    }
    logger.fine(() -> "page cache miss " + key);
    ItemStack[] built = copy(loader.get());
    cache.put(key, built);
    return copy(built);
  }

  private ItemStack[] copy(ItemStack[] source) {
    return Arrays.stream(source).map(this::copy).toArray(ItemStack[]::new);
  }

  private ItemStack copy(ItemStack item) {
    return item == null ? null : item.clone();
  }
}
