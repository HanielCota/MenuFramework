package dev.haniel.menu.paper.view;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.holder.MenuHolder;
import org.bukkit.entity.Player;

/** A static menu: every open reuses the same pre-rendered template. */
public final class StaticPaperMenu implements PaperMenu {

  private final MenuId menuId;
  private final MenuView view;

  /**
   * Wraps a rendered static view.
   *
   * @param menuId the id of this menu; never null
   * @param view the title and pre-rendered template; never null
   */
  public StaticPaperMenu(MenuId menuId, MenuView view) {
    this.menuId = menuId;
    this.view = view;
  }

  @Override
  public void open(Player player) {
    MenuHolder holder = new MenuHolder(menuId, view.template(), view.title());
    player.openInventory(holder.getInventory());
  }
}
