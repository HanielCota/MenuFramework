package dev.haniel.menu.example.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.example.service.MenuReloader;
import dev.haniel.menu.paper.api.MenuClick;

/** Static example menu whose appearance is configured in {@code menus/main.yml}. */
@Menu(id = "main", permission = ExampleMenu.MAIN_PERMISSION)
public final class MainMenu {

  private final MenuReloader reloader;

  public MainMenu(MenuReloader reloader) {
    this.reloader = reloader;
  }

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    if (!click.player().hasPermission(ExampleMenu.CATALOG_PERMISSION)) {
      click.message("<red>You do not have permission to open this menu.</red>");
      return;
    }
    click.open(CatalogMenu.class);
  }

  @Button(id = "reload")
  public void reload(MenuClick click) {
    reloader.reloadAll(click.player());
  }
}
