package dev.haniel.menu.example.service;

import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.paper.MenuFramework;
import org.bukkit.entity.Player;

/** Opens framework menus while keeping permission checks in one place. */
public final class MenuNavigator {

  private final MenuMessages messages;
  private MenuFramework framework;

  public MenuNavigator(MenuMessages messages) {
    this.messages = messages;
  }

  public void attach(MenuFramework framework) {
    this.framework = framework;
  }

  public void openMain(Player player) {
    open(player, ExampleMenu.MAIN);
  }

  public void openCatalog(Player player) {
    open(player, ExampleMenu.CATALOG);
  }

  private void open(Player player, ExampleMenu menu) {
    if (!player.hasPermission(menu.permission())) {
      messages.send(player, "<red>You do not have permission to open this menu.</red>");
      return;
    }
    if (framework == null) {
      messages.send(player, "<red>Menu framework is not ready.</red>");
      return;
    }
    framework.open(player, menu.id());
  }
}
