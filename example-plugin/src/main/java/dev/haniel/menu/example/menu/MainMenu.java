package dev.haniel.menu.example.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.example.service.MenuNavigator;
import dev.haniel.menu.example.service.MenuReloader;
import dev.haniel.menu.paper.api.MenuClick;

/** Static example menu whose appearance is configured in {@code menus/main.yml}. */
@Menu(id = "main")
public final class MainMenu {

  private final MenuNavigator navigator;
  private final MenuReloader reloader;

  public MainMenu(MenuNavigator navigator, MenuReloader reloader) {
    this.navigator = navigator;
    this.reloader = reloader;
  }

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    navigator.openCatalog(click.player());
  }

  @Button(id = "reload")
  public void reload(MenuClick click) {
    reloader.reloadAll(click.player());
  }
}
