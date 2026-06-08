package dev.haniel.menu.paper.holder;

import dev.haniel.menu.domain.MenuId;
import org.bukkit.inventory.InventoryHolder;

/**
 * A held menu view that can name itself and re-render on demand.
 *
 * <p>Implemented by the static and paginated holders so the framework can recognise a player's open
 * menu from {@code getOpenInventory().getTopInventory().getHolder()} — the open inventory is the
 * single source of truth, so no per-player view registry is kept.
 */
public interface OpenMenu extends InventoryHolder {

  /**
   * Returns the id of the menu this view shows.
   *
   * @return the menu id; never null
   */
  MenuId menuId();

  /**
   * Re-renders this view's dynamic content. A no-op for static menus, which have none.
   *
   * <p>Re-runs the {@code @Paginated} provider and re-renders by diff, like a reactive state
   * change. Must be called from the view's owning thread (the main thread on Paper).
   */
  void refresh();
}
