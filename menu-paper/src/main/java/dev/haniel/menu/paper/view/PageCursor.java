package dev.haniel.menu.paper.view;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.paper.reactive.DiffWriter;
import dev.haniel.menu.paper.render.model.RenderedPage;
import java.util.Optional;
import org.bukkit.inventory.Inventory;

/**
 * The per-player state of an open paginated view: its diff writer, current page and click actions.
 *
 * <p>Applying a rendered page writes only the changed slots into the existing inventory and records
 * the page and its action map for O(1) click routing.
 */
public final class PageCursor {

  private final DiffWriter writer;
  private PageNumber page;
  private MenuAction[] actions;
  private boolean hasPrevious;
  private boolean hasNext;

  /**
   * Starts a cursor on the first page over the given inventory.
   *
   * @param inventory the view's inventory; never null
   */
  public PageCursor(Inventory inventory) {
    this.writer = new DiffWriter(inventory);
    this.page = PageNumber.first();
    this.actions = new MenuAction[inventory.getSize()];
  }

  /**
   * Returns the backing inventory.
   *
   * @return the inventory
   */
  public Inventory inventory() {
    return writer.inventory();
  }

  /**
   * Returns the current page.
   *
   * @return the page number
   */
  public PageNumber page() {
    return page;
  }

  /**
   * Finds the action bound to the given raw slot on the current page.
   *
   * @param slot the raw slot
   * @return the action, or empty if out of bounds or unbound
   */
  public Optional<MenuAction> actionAt(int slot) {
    if (slot < 0 || slot >= actions.length) {
      return Optional.empty();
    }
    return Optional.ofNullable(actions[slot]);
  }

  /**
   * Tells whether the current page can move backward.
   *
   * @return {@code true} when a previous page exists
   */
  public boolean hasPrevious() {
    return hasPrevious;
  }

  /**
   * Tells whether the current page can move forward.
   *
   * @return {@code true} when a next page exists
   */
  public boolean hasNext() {
    return hasNext;
  }

  /**
   * Applies a rendered page: records page and actions, writes changed slots in place.
   *
   * @param rendered the page to apply; never null
   */
  public void apply(RenderedPage rendered) {
    this.page = rendered.page();
    this.actions = rendered.actions();
    this.hasPrevious = rendered.hasPrevious();
    this.hasNext = rendered.hasNext();
    writer.write(rendered.slots());
  }
}
