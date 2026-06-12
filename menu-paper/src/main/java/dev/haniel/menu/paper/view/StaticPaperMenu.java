package dev.haniel.menu.paper.view;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.holder.MenuHolder;
import dev.haniel.menu.paper.visibility.StaticVisibility;
import java.util.Objects;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** A static menu: every open reuses the same pre-rendered template. */
public final class StaticPaperMenu implements PaperMenu {

  private final MenuId menuId;
  private final MenuView view;
  private final StaticVisibility visibility;

  /**
   * Wraps a rendered static view with no per-viewer visibility rules.
   *
   * @param menuId the id of this menu; never null
   * @param view the title and pre-rendered template; never null
   */
  public StaticPaperMenu(MenuId menuId, MenuView view) {
    this(menuId, view, StaticVisibility.none());
  }

  private StaticPaperMenu(MenuId menuId, MenuView view, StaticVisibility visibility) {
    this.menuId = Objects.requireNonNull(menuId, "menuId");
    this.view = Objects.requireNonNull(view, "view");
    this.visibility = Objects.requireNonNull(visibility, "visibility");
  }

  /**
   * Returns a copy of this menu that hides buttons per the given {@code @Visible} rules.
   *
   * @param visibility the per-viewer visibility to apply; never null
   * @return a static menu wired with the visibility
   */
  public StaticPaperMenu withVisibility(StaticVisibility visibility) {
    return new StaticPaperMenu(menuId, view, Objects.requireNonNull(visibility, "visibility"));
  }

  @Override
  public void open(Player player, @Nullable Object argument) {
    Set<Integer> hidden = visibility.hiddenSlots(player);
    MenuHolder holder = new MenuHolder(menuId, view.template(), view.title(), hidden);
    player.openInventory(holder.getInventory());
  }
}
