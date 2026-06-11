package dev.haniel.menu.paper.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.Page;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Probes for {@link PageRenderer#renderPage}, the lazy assembly path: items come straight from the
 * loaded {@link Page}, the next flag comes from the page, the previous flag from the page number,
 * and items beyond the content capacity are trimmed rather than overflowing.
 */
class PageRendererLazyTest {

  // Mask: previous '<' at slot 0, two content 'X' at 1 and 2, next '>' at slot 3.
  private static final List<String> MASK = List.of("<XX>     ");

  @Test
  void placesLoadedItemsInContentSlots() {
    RenderedPage page = renderer().renderPage(PageNumber.first(), Page.of(items(2), false));

    assertNotNull(page.slots()[1], "first content slot holds the first loaded item");
    assertNotNull(page.slots()[2], "second content slot holds the second loaded item");
  }

  @Test
  void nextFlagAndDecorComeFromTheLoadedPage() {
    RenderedPage more = renderer().renderPage(PageNumber.first(), Page.of(items(2), true));
    assertTrue(more.hasNext(), "hasNext must reflect the page that reported a next");
    assertNotNull(more.slots()[3], "next decor is drawn when the page reports a next");

    RenderedPage last = renderer().renderPage(PageNumber.first(), Page.of(items(2), false));
    assertFalse(last.hasNext(), "hasNext must be false when the page reports no next");
    assertNull(last.slots()[3], "no next decor when the page reports no next");
  }

  @Test
  void previousFlagAndDecorComeFromThePageNumber() {
    RenderedPage first = renderer().renderPage(PageNumber.first(), Page.of(items(1), true));
    assertFalse(first.hasPrevious(), "the first page has no previous");
    assertNull(first.slots()[0], "no previous decor on the first page");

    RenderedPage second = renderer().renderPage(new PageNumber(1), Page.of(items(1), false));
    assertTrue(second.hasPrevious(), "a page after the first has a previous");
    assertNotNull(second.slots()[0], "previous decor is drawn past the first page");
  }

  @Test
  void itemsBeyondTheContentCapacityAreTrimmed() {
    // Two content slots, but the provider returns three items: the extra must be dropped, not
    // crash.
    RenderedPage page = renderer().renderPage(PageNumber.first(), Page.of(items(3), false));

    assertNotNull(page.slots()[1]);
    assertNotNull(page.slots()[2]);
  }

  private PageRenderer renderer() {
    PageScene scene =
        new PageScene(
            new MenuId("lazy"),
            Component.text("Lazy"),
            9,
            MaskLayout.resolve(MASK, 1),
            new PagedDecor<>(stack(Material.ARROW), stack(Material.SPECTRAL_ARROW), null),
            new PagedContent<>(ContentProvider.empty(), icon -> stack(Material.STONE)),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererLazyTest.class.getName())),
        new DataVersion(),
        (holder, size, title) -> {
          throw new UnsupportedOperationException("inventory not needed for render test");
        });
  }

  private static List<MenuItem> items(int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> MenuItem.of(Icon.of(Material.STONE.name())))
        .toList();
  }

  private static ItemStack stack(Material material) {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> stack(material));
    when(item.getType()).thenReturn(material);
    return item;
  }
}
