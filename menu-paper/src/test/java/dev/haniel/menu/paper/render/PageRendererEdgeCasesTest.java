package dev.haniel.menu.paper.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.logging.Logger;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Off-by-one and boundary probes for {@link PageRenderer}: exact multiples of {@code perPage}, the
 * empty-content single page, clamping a far-future page, and the navigation flags at the exact
 * page-count boundary. These hunt for {@code hasNext} being true on the true last page and for the
 * full-vs-partial content fill at the perPage edge.
 */
class PageRendererEdgeCasesTest {

  // Two content slots ('X') at 1 and 2, previous '<' at 0, next '>' at 3.
  private static final List<String> MASK = List.of("<XX>     ");

  @Test
  void exactlyOneFullPageReportsNoNextPage() {
    // 2 items, perPage 2 -> exactly one page; hasNext must be false (no off-by-one spillover).
    RenderedPage page = renderer(2).render(PageNumber.first());

    assertFalse(page.hasNext(), "a single exactly-full page must not advertise a next page");
    assertNull(page.slots()[3], "no next decor when there is no next page");
    assertNotNull(page.slots()[1], "first content slot filled");
    assertNotNull(page.slots()[2], "second content slot filled on a full page");
  }

  @Test
  void exactMultipleHasNextOnFirstButNotOnLastPage() {
    // 4 items, perPage 2 -> pages 0 and 1, the last exactly full.
    PageRenderer renderer = renderer(4);

    RenderedPage first = renderer.render(PageNumber.first());
    assertTrue(first.hasNext(), "page 0 of an exact multiple has a next page");
    assertFalse(first.hasPrevious());

    RenderedPage last = renderer.render(new PageNumber(1));
    assertFalse(last.hasNext(), "the exactly-full last page must not advertise a next page");
    assertTrue(last.hasPrevious());
    assertNotNull(last.slots()[2], "the last page is full, so its trailing content slot is filled");
  }

  @Test
  void lastPageContentMatchesTheLastSliceNotTheFirst() {
    // 4 distinct items, perPage 2: page 1 must hold items 2 and 3, not 0 and 1.
    RenderedPage page = distinctRenderer().render(new PageNumber(1));

    assertEquals(Material.DIAMOND, page.slots()[1].getType(), "page 1 first slot is item index 2");
    assertEquals(Material.EMERALD, page.slots()[2].getType(), "page 1 second slot is item index 3");
  }

  @Test
  void clampingAFarFuturePageRendersTheLastSliceAndFlags() {
    // 3 items, perPage 2 -> 2 pages (0,1); requesting a huge page must clamp to page 1.
    RenderedPage page = renderer(3).render(new PageNumber(Integer.MAX_VALUE));

    assertEquals(1, page.page().value(), "a far-future page clamps to the last page index");
    assertFalse(page.hasNext(), "clamped last page reports no next page");
    assertTrue(page.hasPrevious(), "clamped last page reports a previous page");
    assertNotNull(page.slots()[1], "the single trailing item sits in the first content slot");
    assertNull(page.slots()[2], "the unused trailing slot stays empty on the partial last page");
  }

  @Test
  void emptyContentClampsToPageZeroWithNoNavigation() {
    // No items -> totalPages is 1; even a large request clamps to page 0.
    RenderedPage page = renderer(0).render(new PageNumber(7));

    assertEquals(0, page.page().value(), "empty content has only page 0 to clamp to");
    assertFalse(page.hasNext(), "empty content has no next page");
    assertFalse(page.hasPrevious(), "empty content has no previous page");
    assertNull(page.slots()[0], "no previous decor");
    assertNull(page.slots()[3], "no next decor");
  }

  @Test
  void middlePageOfThreeShowsBothNavigationControls() {
    // 5 items, perPage 2 -> 3 pages; page 1 is a true middle page with both controls.
    RenderedPage page = renderer(5).render(new PageNumber(1));

    assertTrue(page.hasPrevious(), "a middle page has a previous page");
    assertTrue(page.hasNext(), "a middle page has a next page");
    assertNotNull(page.slots()[0], "previous decor drawn on a middle page");
    assertNotNull(page.slots()[3], "next decor drawn on a middle page");
  }

  private PageRenderer renderer(int items) {
    return rendererFor(
        IntStream.range(0, items)
            .mapToObj(index -> MenuItem.of(Icon.of(Material.STONE.name())))
            .toList());
  }

  private PageRenderer distinctRenderer() {
    return rendererFor(
        List.of(
            MenuItem.of(Icon.of(Material.IRON_INGOT.name())),
            MenuItem.of(Icon.of(Material.GOLD_INGOT.name())),
            MenuItem.of(Icon.of(Material.DIAMOND.name())),
            MenuItem.of(Icon.of(Material.EMERALD.name()))));
  }

  private PageRenderer rendererFor(List<MenuItem> items) {
    PageScene scene =
        new PageScene(
            new MenuId("shop"),
            Component.text("Shop"),
            9,
            MaskLayout.resolve(MASK, 1),
            new PagedDecor<>(stack(Material.ARROW), stack(Material.SPECTRAL_ARROW), null),
            new PagedContent<>(provider(items), icon -> stack(Material.matchMaterial(icon.material()))),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererEdgeCasesTest.class.getName())),
        new DataVersion(),
        (holder, size, title) -> {
          throw new UnsupportedOperationException("inventory not needed for render test");
        });
  }

  private static ContentProvider provider(List<MenuItem> items) {
    Fixed source = new Fixed(items);
    try {
      return new ContentProvider(
          MethodHandles.lookup().unreflect(Fixed.class.getDeclaredMethod("items")).bindTo(source));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static ItemStack stack(Material material) {
    ItemStack item = mock(ItemStack.class);
    when(item.clone()).thenAnswer(ignored -> stack(material));
    when(item.getType()).thenReturn(material);
    return item;
  }

  public static final class Fixed {
    private final List<MenuItem> items;

    public Fixed(List<MenuItem> items) {
      this.items = List.copyOf(items);
    }

    public List<MenuItem> items() {
      return items;
    }
  }
}
