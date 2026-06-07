package dev.haniel.menu.item;

import dev.haniel.menu.action.MenuAction;
import java.util.Objects;

/**
 * An immutable content entry: an appearance paired with a click action.
 *
 * <p>Built in code to feed paginated menus, where both the look and the behaviour of each entry are
 * dynamic. Created through {@link #of(Icon)} with a no-op action; {@link #onClick(MenuAction)}
 * returns a new item rather than mutating this one.
 */
public final class MenuItem {

  private final Icon icon;
  private final MenuAction action;

  private MenuItem(Icon icon, MenuAction action) {
    this.icon = Objects.requireNonNull(icon, "icon");
    this.action = Objects.requireNonNull(action, "action");
  }

  /**
   * Creates an item with the given appearance and a no-op action.
   *
   * @param icon the appearance of the entry; never null
   * @return a new item that does nothing when clicked
   */
  public static MenuItem of(Icon icon) {
    return new MenuItem(icon, context -> {});
  }

  /**
   * Returns a copy of this item that runs the given action when clicked.
   *
   * @param action the action to run on click; never null
   * @return a new item carrying the action
   */
  public MenuItem onClick(MenuAction action) {
    return new MenuItem(icon, action);
  }

  /**
   * Returns the appearance of this item.
   *
   * @return the icon; never null
   */
  public Icon icon() {
    return icon;
  }

  /**
   * Returns the action of this item.
   *
   * @return the action; never null
   */
  public MenuAction action() {
    return action;
  }
}
