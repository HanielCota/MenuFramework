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
 * Adversarial clamp and navigation probes for {@link PageRenderer}: a page requested past the end
 * must clamp to the last page and report navigation flags for the clamped page, the previous/next
 * decor must appear only when the move is possible, and the content must respect the slice.
 */
class PageRendererNavigationTest {

  // Mask: previous '<' at slot 0, two content 'X' at 1 and 2, next '>' at slot 3.
  private static final List<String> MASK = List.of("<XX>     ");

  @Test
  void requestingPagePastTheEndClampsToLastPage() {
    RenderedPage page = renderer(5).render(new PageNumber(99));

    // 5 items, 2 per page -> 3 pages (0,1,2); last page index is 2.
    assertEquals(2, page.page().value(), "a page past the end must clamp to the last page");
    assertFalse(page.hasNext(), "the last page must report no next page");
    assertTrue(page.hasPrevious(), "the last page must report a previous page");
  }

  @Test
  void firstPageOfManyShowsNextDecorButNotPrevious() {
    RenderedPage page = renderer(5).render(PageNumber.first());

    assertTrue(page.hasNext());
    assertFalse(page.hasPrevious());
    assertNull(page.slots()[0], "no previous decor on the first page");
    assertNotNull(page.slots()[3], "next decor must be drawn when a next page exists");
  }

  @Test
  void lastPageShowsPreviousDecorButNotNext() {
    RenderedPage page = renderer(5).render(new PageNumber(2));

    assertNotNull(page.slots()[0], "previous decor must be drawn when a previous page exists");
    assertNull(page.slots()[3], "no next decor on the last page");
  }

  @Test
  void clampedPageRendersTheLastPageContentNotTheRequestedSlice() {
    // 3 items, 2 per page -> pages: [0,1] and [2]. Last page holds exactly one content item.
    RenderedPage page = renderer(3).render(new PageNumber(50));

    assertEquals(1, page.page().value());
    assertNotNull(
        page.slots()[1], "the single item of the last page sits in the first content slot");
    assertNull(page.slots()[2], "the unused content slot on the last page stays empty");
  }

  private PageRenderer renderer(int items) {
    PageScene scene =
        new PageScene(
            new MenuId("shop"),
            Component.text("Shop"),
            9,
            MaskLayout.resolve(MASK, 1),
            new PagedDecor<>(stack(Material.ARROW), stack(Material.SPECTRAL_ARROW), null),
            new PagedContent<>(provider(items), icon -> stack(Material.STONE)),
            new Overlay(Map.of(), Map.of()));
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererNavigationTest.class.getName())),
        new DataVersion(),
        (holder, size, title) -> {
          throw new UnsupportedOperationException("inventory not needed for render test");
        });
  }

  private static ContentProvider provider(int count) {
    Fixed source = new Fixed(count);
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
    private final int count;

    public Fixed(int count) {
      this.count = count;
    }

    public List<MenuItem> items() {
      return IntStream.range(0, count)
          .mapToObj(index -> MenuItem.of(Icon.of(Material.STONE.name())))
          .toList();
    }
  }
}
