package dev.haniel.menu.paper.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.model.Overlay;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.paper.render.model.RenderedPage;
import dev.haniel.menu.template.PagedContent;
import dev.haniel.menu.template.PagedDecor;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial cache-correctness probes for {@link PageRenderer}: name/lore-only icon changes must
 * miss the visual cache, {@link PageRenderer#invalidate()} must force a rebuild, and an unchanged
 * page must reuse the cached visuals (a hit, not a rebuild).
 */
class PageRendererCacheTest {

  @Test
  void nameOnlyIconChangeMissesCache() throws Exception {
    MutableContent source = new MutableContent();
    AtomicInteger renders = new AtomicInteger();
    PageRenderer renderer = renderer(source, renders);

    renderer.render(PageNumber.first());
    int afterFirst = renders.get();
    source.rename("renamed");
    renderer.render(PageNumber.first());

    // The icon name changed but material is identical: the visual must be rebuilt, not reused.
    assertEquals(afterFirst + 1, renders.get(), "name-only change must rebuild the visual");
  }

  @Test
  void loreOnlyIconChangeMissesCache() throws Exception {
    MutableContent source = new MutableContent();
    AtomicInteger renders = new AtomicInteger();
    PageRenderer renderer = renderer(source, renders);

    renderer.render(PageNumber.first());
    int afterFirst = renders.get();
    source.relore(List.of("a brand new lore line"));
    renderer.render(PageNumber.first());

    assertEquals(afterFirst + 1, renders.get(), "lore-only change must rebuild the visual");
  }

  @Test
  void unchangedPageHitsCacheAndDoesNotRebuild() throws Exception {
    MutableContent source = new MutableContent();
    AtomicInteger renders = new AtomicInteger();
    PageRenderer renderer = renderer(source, renders);

    renderer.render(PageNumber.first());
    int afterFirst = renders.get();
    renderer.render(PageNumber.first());

    assertEquals(afterFirst, renders.get(), "unchanged content must be served from cache");
  }

  @Test
  void invalidateForcesRebuildEvenWhenContentIsIdentical() throws Exception {
    MutableContent source = new MutableContent();
    AtomicInteger renders = new AtomicInteger();
    PageRenderer renderer = renderer(source, renders);

    renderer.render(PageNumber.first());
    int afterFirst = renders.get();
    renderer.invalidate();
    renderer.render(PageNumber.first());

    assertEquals(afterFirst + 1, renders.get(), "invalidate() must bypass the cache");
  }

  @Test
  void overlayItemIsPlacedOnTopOfContent() throws Exception {
    MutableContent source = new MutableContent();
    ItemStack overlayItem = stack(Material.BEACON);
    PageRenderer renderer =
        renderer(source, new AtomicInteger(), new Overlay(Map.of(0, overlayItem), Map.of()));

    RenderedPage page = renderer.render(PageNumber.first());

    // The overlay is documented to draw on top; slot 0 must be the overlay, not the content item.
    assertSame(overlayItem, page.slots()[0]);
  }

  private PageRenderer renderer(MutableContent source, AtomicInteger renders) throws Exception {
    return renderer(source, renders, new Overlay(Map.of(), Map.of()));
  }

  private PageRenderer renderer(MutableContent source, AtomicInteger renders, Overlay overlay)
      throws Exception {
    ContentProvider provider =
        new ContentProvider(
            MethodHandles.lookup()
                .unreflect(MutableContent.class.getDeclaredMethod("items"))
                .bindTo(source));
    PageScene scene =
        new PageScene(
            new MenuId("shop"),
            Component.text("Shop"),
            9,
            MaskLayout.resolve(List.of("X        "), 1),
            new PagedDecor<>(null, null, stack(Material.GRAY_STAINED_GLASS_PANE)),
            new PagedContent<>(
                provider,
                icon -> {
                  renders.incrementAndGet();
                  return stack(Material.matchMaterial(icon.material()));
                }),
            overlay);
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererCacheTest.class.getName())),
        new DataVersion(),
        (holder, size, title) -> {
          throw new UnsupportedOperationException("inventory not needed for render test");
        });
  }

  private static ItemStack stack(Material material) {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> stack(material));
    when(item.getType()).thenReturn(material);
    return item;
  }

  public static final class MutableContent {
    private Icon icon = Icon.of(Material.STONE.name());

    public List<MenuItem> items() {
      return List.of(MenuItem.of(icon));
    }

    void rename(String name) {
      icon = icon.named(name);
    }

    void relore(List<String> lore) {
      icon = icon.describedBy(lore);
    }
  }
}
