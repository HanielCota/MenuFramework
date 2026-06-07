package dev.haniel.menu.paper.render.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class PageCacheTest {

  private final PageCache cache = new PageCache(Logger.getLogger(PageCacheTest.class.getName()));

  @Test
  void returnsDefensiveCopiesOfCachedItems() {
    AtomicInteger loads = new AtomicInteger();
    PageKey key = new PageKey(new MenuId("shop"), 0, 0, 1);

    ItemStack[] first =
        cache.get(
            key,
            () -> {
              loads.incrementAndGet();
              return new ItemStack[] {item("stone")};
            });
    first[0] = item("dirty");

    ItemStack[] second = cache.get(key, () -> new ItemStack[] {item("diamond")});

    assertEquals(1, loads.get());
    assertEquals("stone", second[0].toString());
    assertNotSame(first[0], second[0]);
  }

  @Test
  void changingContentHashMissesCache() {
    AtomicInteger loads = new AtomicInteger();
    MenuId id = new MenuId("shop");

    cache.get(
        new PageKey(id, 0, 0, 1),
        () -> {
          loads.incrementAndGet();
          return new ItemStack[] {item("stone")};
        });
    ItemStack[] changed =
        cache.get(
            new PageKey(id, 0, 0, 2),
            () -> {
              loads.incrementAndGet();
              return new ItemStack[] {item("dirt")};
            });

    assertEquals(2, loads.get());
    assertEquals("dirt", changed[0].toString());
  }

  private static ItemStack item(String name) {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> item(name));
    when(item.toString()).thenReturn(name);
    return item;
  }
}
