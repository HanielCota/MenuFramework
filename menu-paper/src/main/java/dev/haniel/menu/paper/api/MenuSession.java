package dev.haniel.menu.paper.api;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.holder.OpenMenu;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * A handle to the menu a player currently has open, obtained from {@code MenuFramework.session}.
 *
 * <p>Lets outside code react to domain events without holding a reference to the menu instance:
 * re-render it, close it, or read which menu it is. It is a transient snapshot of what is open now
 * — query it again rather than caching it, since the player may close or switch menus at any time.
 *
 * <p>Call these on the viewer's owning thread, as they touch the Bukkit API: the main server thread
 * on Paper, the player's region thread on Folia.
 */
public final class MenuSession {

  private final Player viewer;
  private final OpenMenu menu;

  /**
   * Wraps the given player's open menu.
   *
   * @param viewer the player viewing the menu; never null
   * @param menu the open menu view; never null
   */
  public MenuSession(Player viewer, OpenMenu menu) {
    this.viewer = Objects.requireNonNull(viewer, "viewer");
    this.menu = Objects.requireNonNull(menu, "menu");
  }

  /**
   * Returns the id of the open menu.
   *
   * @return the menu id; never null
   */
  public MenuId menuId() {
    return menu.menuId();
  }

  /**
   * Re-renders the open menu's dynamic content (a no-op for static menus).
   *
   * <p>Use this to reflect a domain change the menu reads but does not own, such as a balance
   * updated by another system. State the menu owns should still change through a {@code @Reactive}
   * field.
   */
  public void refresh() {
    menu.refresh();
  }

  /** Closes the open menu for the player. */
  public void close() {
    viewer.closeInventory();
  }
}
