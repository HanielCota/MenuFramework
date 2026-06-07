package dev.haniel.menu.paper.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.action.MenuAction;
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

class PageRendererTest {

  @Test
  void contentIconChangeMissesCacheEvenWithoutExplicitInvalidation() throws Exception {
    MutableContent source = new MutableContent();
    PageRenderer renderer = renderer(source);

    RenderedPage first = renderer.render(PageNumber.first());
    source.next();
    RenderedPage second = renderer.render(PageNumber.first());

    assertEquals(Material.STONE, first.slots()[0].getType());
    assertEquals(Material.DIRT, second.slots()[0].getType());
    second.actions()[0].onClick(null);
    assertEquals(1, source.clicks());
  }

  private PageRenderer renderer(MutableContent source) throws Exception {
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
            new PagedContent<>(provider, item -> stack(Material.matchMaterial(item.material()))),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererTest.class.getName())),
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
    private Material material = Material.STONE;
    private final AtomicInteger clicks = new AtomicInteger();

    public List<MenuItem> items() {
      MenuAction action = ignored -> clicks.incrementAndGet();
      return List.of(MenuItem.of(Icon.of(material.name())).onClick(action));
    }

    void next() {
      material = Material.DIRT;
    }

    int clicks() {
      return clicks.get();
    }
  }
}
