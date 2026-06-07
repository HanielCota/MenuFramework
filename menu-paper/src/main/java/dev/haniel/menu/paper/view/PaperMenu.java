package dev.haniel.menu.paper.view;

import org.bukkit.entity.Player;

/** An openable menu, either static or paginated, registered under a {@code MenuId}. */
public interface PaperMenu {

  /**
   * Opens this menu for the given player.
   *
   * @param player the viewer; never null
   */
  void open(Player player);
}
