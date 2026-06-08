package dev.haniel.menu.paper.render;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.domain.Paginator;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.cache.PageKey;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.paper.render.model.RenderedPage;
import dev.haniel.menu.template.PagedDecor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Renders a page of a paginated menu: provider call, slice, cached visuals, slot assembly.
 *
 * <p>The provider is invoked every render for fresh actions and content (a content change is
 * reflected even without an explicit invalidation); only the content visuals go through the
 * per-view {@link PageCache}. {@link #invalidate()} bumps the data version so a state change makes
 * the current page's cached visuals miss and rebuild. The requested page is clamped to the valid
 * range.
 *
 * <p>Not thread-safe: one renderer serves one open view and runs on that view's owning thread.
 */
public final class PageRenderer {

  private final PageScene scene;
  private final PageCache cache;
  private final DataVersion version;
  private final InventoryFactory inventories;

  /**
   * Wires a renderer for one open view.
   *
   * @param scene the per-view render inputs; never null
   * @param cache the view's rendered-page cache; never null
   * @param version the view's source data version; never null
   * @param inventories the platform inventory factory; never null
   */
  public PageRenderer(
      PageScene scene, PageCache cache, DataVersion version, InventoryFactory inventories) {
    this.scene = scene;
    this.cache = cache;
    this.version = version;
    this.inventories = inventories;
  }

  /**
   * Creates the empty inventory backing this view.
   *
   * @param holder the holder to bind the inventory to; never null
   * @return a new, empty inventory of the right size and title
   */
  public Inventory newInventory(InventoryHolder holder) {
    return inventories.create(holder, scene.size(), scene.title());
  }

  /**
   * Returns the resolved mask layout.
   *
   * @return the layout
   */
  public MaskLayout layout() {
    return scene.layout();
  }

  /**
   * Returns the id of the menu being rendered.
   *
   * @return the menu id
   */
  public MenuId menuId() {
    return scene.id();
  }

  /** Invalidates cached pages so the next render rebuilds from current data. */
  public void invalidate() {
    version.bump();
  }

  /**
   * Renders the requested page, clamping it to the valid range.
   *
   * @param requested the page to show
   * @return the assembled, ready-to-write page
   */
  public RenderedPage render(PageNumber requested) {
    Paginator paginator = new Paginator(scene.content().items());
    PageNumber page = clamp(requested, paginator);
    List<MenuItem> items = paginator.page(page, scene.perPage());
    ItemStack[] visuals = cache.get(key(page, items), () -> renderVisuals(items));
    return assemble(page, items, visuals, paginator);
  }

  private RenderedPage assemble(
      PageNumber page, List<MenuItem> items, ItemStack[] visuals, Paginator paginator) {
    ItemStack[] slots = new ItemStack[scene.size()];
    MenuAction[] actions = new MenuAction[scene.size()];
    fillBorder(slots);
    fillContent(slots, actions, items, visuals);
    placeNavigation(slots, page, paginator);
    placeOverlay(slots, actions);
    return new RenderedPage(
        page,
        slots,
        actions,
        paginator.hasPrevious(page),
        paginator.hasNext(page, scene.perPage()));
  }

  private void fillBorder(ItemStack[] slots) {
    PagedDecor<ItemStack> decor = scene.decor();
    Arrays.stream(layout().borderSlots()).forEach(slot -> slots[slot] = decor.border());
  }

  private void fillContent(
      ItemStack[] slots, MenuAction[] actions, List<MenuItem> items, ItemStack[] visuals) {
    int[] contentSlots = layout().contentSlots();
    IntStream.range(0, items.size())
        .forEach(
            index -> place(slots, actions, contentSlots[index], visuals[index], items.get(index)));
  }

  private void place(
      ItemStack[] slots, MenuAction[] actions, int slot, ItemStack visual, MenuItem item) {
    slots[slot] = visual;
    actions[slot] = item.action();
  }

  private void placeNavigation(ItemStack[] slots, PageNumber page, Paginator paginator) {
    placePrevious(slots, page, paginator);
    placeNext(slots, page, paginator);
  }

  private void placePrevious(ItemStack[] slots, PageNumber page, Paginator paginator) {
    int slot = layout().previousSlot();
    if (slot < 0 || !paginator.hasPrevious(page)) {
      return;
    }
    slots[slot] = scene.decor().previous();
  }

  private void placeNext(ItemStack[] slots, PageNumber page, Paginator paginator) {
    int slot = layout().nextSlot();
    if (slot < 0 || !paginator.hasNext(page, scene.perPage())) {
      return;
    }
    slots[slot] = scene.decor().next();
  }

  private void placeOverlay(ItemStack[] slots, MenuAction[] actions) {
    scene.overlay().visuals().forEach((slot, item) -> slots[slot] = item);
    scene.overlay().actions().forEach((slot, action) -> actions[slot] = action);
  }

  private ItemStack[] renderVisuals(List<MenuItem> items) {
    return items.stream().map(scene.content()::render).toArray(ItemStack[]::new);
  }

  private PageNumber clamp(PageNumber requested, Paginator paginator) {
    int lastPage = paginator.totalPages(scene.perPage()) - 1;
    return new PageNumber(Math.clamp(requested.value(), 0, lastPage));
  }

  private PageKey key(PageNumber page, List<MenuItem> items) {
    return new PageKey(scene.id(), page.value(), version.current(), contentHash(items));
  }

  /**
   * Builds a hash of the page's item icons for cache keying.
   *
   * <p>Note: this hash only considers {@link Icon}; two {@link MenuItem}s with the same icon but
   * different actions will collide. This is acceptable because the cache stores visuals only —
   * actions are resolved separately on every render.
   */
  private int contentHash(List<MenuItem> items) {
    return items.stream().map(MenuItem::icon).mapToInt(Icon::hashCode).reduce(1, this::combineHash);
  }

  private int combineHash(int current, int next) {
    return 31 * current + next;
  }
}
