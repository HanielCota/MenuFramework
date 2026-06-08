package dev.haniel.menu.paper.api;

import dev.haniel.menu.domain.MenuId;
import org.bukkit.entity.Player;

/**
 * Opens a registered menu for a player.
 *
 * <p>The framework's own navigation entry point, exposed to {@link MenuClick} so a button can open
 * another menu without the caller wiring a late reference to the framework. Opening a menu the
 * player may not see (missing permission) or that is not registered is a silent no-op, matching the
 * framework's open semantics.
 */
public interface MenuOpener {

  /**
   * Opens the menu with the given id for the player.
   *
   * @param viewer the player to open the menu for; never null
   * @param id the registered menu id; never null
   */
  void open(Player viewer, MenuId id);

  /**
   * Opens the menu registered from the given class for the player.
   *
   * @param viewer the player to open the menu for; never null
   * @param menuType the registered menu class; never null
   */
  void open(Player viewer, Class<?> menuType);
}
