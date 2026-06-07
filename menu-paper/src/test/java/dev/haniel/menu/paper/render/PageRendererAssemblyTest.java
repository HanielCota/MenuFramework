package dev.haniel.menu.paper.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial assembly probes for {@link PageRenderer}: the order in which the sliced content lands
 * in the mask's content slots, border/content/overlay layering, and the absence of ghost items when
 * the page is empty or partially filled. These exercise the content-slot-to-slice-index mapping in
 * {@code fillContent} against a non-contiguous, interleaved mask.
 */
class PageRendererAssemblyTest {

  // '#' border (0), 'X' content (1), '#' (2), 'X' content (3), '<' prev (4),
  // 'X' content (5), '>' next (6), '#' (7), 'X' content (8). Content reading order: 1,3,5,8.
  private static final List<String> INTERLEAVED = List.of("#X#X<X>#X");

  @Test
  void slicedContentLandsInMaskReadingOrderNotSlotOrder() {
    // Four distinct items on a single page; each must land in its content slot, in reading order.
    RenderedPage page = renderer(materials()).render(PageNumber.first());

    assertEquals(Material.IRON_INGOT, page.slots()[1].getType(), "item 0 -> first content slot");
    assertEquals(Material.GOLD_INGOT, page.slots()[3].getType(), "item 1 -> second content slot");
    assertEquals(Material.DIAMOND, page.slots()[5].getType(), "item 2 -> third content slot");
    assertEquals(Material.EMERALD, page.slots()[8].getType(), "item 3 -> fourth content slot");
  }

  @Test
  void borderFillsOnlyBorderSlotsAndNeverOverwritesContent() {
    RenderedPage page = renderer(materials()).render(PageNumber.first());

    assertEquals(Material.GRAY_STAINED_GLASS_PANE, page.slots()[0].getType(), "border slot 0");
    assertEquals(Material.GRAY_STAINED_GLASS_PANE, page.slots()[2].getType(), "border slot 2");
    assertEquals(Material.GRAY_STAINED_GLASS_PANE, page.slots()[7].getType(), "border slot 7");
    assertEquals(
        Material.IRON_INGOT, page.slots()[1].getType(), "content not overwritten by border");
  }

  @Test
  void emptyContentListLeavesEveryContentSlotEmpty() {
    RenderedPage page = renderer(List.of()).render(PageNumber.first());

    assertNull(page.slots()[1], "no ghost item in content slot 1 when content is empty");
    assertNull(page.slots()[3], "no ghost item in content slot 3 when content is empty");
    assertNull(page.slots()[5], "no ghost item in content slot 5 when content is empty");
    assertNull(page.slots()[8], "no ghost item in content slot 8 when content is empty");
  }

  @Test
  void emptyContentStillDrawsBorderAndNoNavigation() {
    RenderedPage page = renderer(List.of()).render(PageNumber.first());

    assertEquals(Material.GRAY_STAINED_GLASS_PANE, page.slots()[0].getType(), "border still drawn");
    assertNull(page.slots()[4], "no previous decor on the only page");
    assertNull(page.slots()[6], "no next decor on the only page");
  }

  @Test
  void partialPageFillsLeadingContentSlotsInOrderAndLeavesTrailingEmpty() {
    // Two items, four content slots: they must occupy the first two in reading order, rest empty.
    RenderedPage page =
        renderer(List.of(Material.IRON_INGOT, Material.GOLD_INGOT)).render(PageNumber.first());

    assertEquals(
        Material.IRON_INGOT, page.slots()[1].getType(), "first item in first content slot");
    assertEquals(
        Material.GOLD_INGOT, page.slots()[3].getType(), "second item in second content slot");
    assertNull(page.slots()[5], "third content slot stays empty on a partial page");
    assertNull(page.slots()[8], "fourth content slot stays empty on a partial page");
  }

  @Test
  void actionsAlignWithTheItemInEachContentSlot() throws Throwable {
    // Distinct actions so a mis-mapping (action of item i landing on item j's slot) is caught.
    int[] fired = new int[1];
    List<MenuItem> items =
        List.of(
            MenuItem.of(Icon.of(Material.IRON_INGOT.name())).onClick(ctx -> fired[0] = 11),
            MenuItem.of(Icon.of(Material.GOLD_INGOT.name())).onClick(ctx -> fired[0] = 22),
            MenuItem.of(Icon.of(Material.DIAMOND.name())).onClick(ctx -> fired[0] = 33),
            MenuItem.of(Icon.of(Material.EMERALD.name())).onClick(ctx -> fired[0] = 44));
    RenderedPage page = rendererFromItems(items).render(PageNumber.first());

    page.actions()[5].onClick(null);
    assertEquals(33, fired[0], "the action in content slot 5 must be item 2's action, not another");
    page.actions()[8].onClick(null);
    assertEquals(44, fired[0], "the action in content slot 8 must be item 3's action");
    assertNull(page.actions()[0], "border slot carries no action");
    assertNull(page.actions()[2], "border slot carries no action");
  }

  @Test
  void overlayDrawsOnTopOfBorderAndCarriesItsAction() throws Throwable {
    int[] fired = new int[1];
    ItemStack overlayItem = stack(Material.BEACON);
    RenderedPage page =
        rendererWithOverlay(
                toItems(materials()),
                new Overlay(Map.of(0, overlayItem), Map.of(0, ctx -> fired[0] = 99)))
            .render(PageNumber.first());

    assertSame(overlayItem, page.slots()[0], "overlay must replace the border on slot 0");
    assertNotNull(page.actions()[0], "overlay action must be bound on its slot");
    page.actions()[0].onClick(null);
    assertEquals(99, fired[0], "overlay action must run when its slot is clicked");
  }

  private PageRenderer renderer(List<Material> contentMaterials) {
    return rendererFromItems(toItems(contentMaterials));
  }

  private static List<MenuItem> toItems(List<Material> contentMaterials) {
    return contentMaterials.stream()
        .map(material -> MenuItem.of(Icon.of(material.name())))
        .toList();
  }

  private PageRenderer rendererFromItems(List<MenuItem> items) {
    return rendererWithOverlay(items, new Overlay(Map.of(), Map.of()));
  }

  private PageRenderer rendererWithOverlay(List<MenuItem> items, Overlay overlay) {
    PageScene scene =
        new PageScene(
            new MenuId("shop"),
            Component.text("Shop"),
            9,
            MaskLayout.resolve(INTERLEAVED, 1),
            new PagedDecor<>(
                stack(Material.ARROW),
                stack(Material.SPECTRAL_ARROW),
                stack(Material.GRAY_STAINED_GLASS_PANE)),
            new PagedContent<>(
                provider(items), icon -> stack(Material.matchMaterial(icon.material()))),
            overlay);
    return new PageRenderer(
        scene,
        new PageCache(Logger.getLogger(PageRendererAssemblyTest.class.getName())),
        new DataVersion(),
        (holder, size, title) -> {
          throw new UnsupportedOperationException("inventory not needed for render test");
        });
  }

  private static List<Material> materials() {
    return List.of(Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD);
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
