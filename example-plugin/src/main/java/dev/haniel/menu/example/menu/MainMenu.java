package dev.haniel.menu.example.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.paper.api.MenuClick;

@Menu(id = "main", permission = "menuexample.open.main")
public final class MainMenu {

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    click.open(CatalogMenu.class);
  }
}
