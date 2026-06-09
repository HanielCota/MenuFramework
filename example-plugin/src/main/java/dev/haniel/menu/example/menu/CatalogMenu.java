package dev.haniel.menu.example.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.example.domain.CatalogProduct;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.state.State;
import java.util.List;

@Menu(id = "catalog", permission = "menuexample.open.catalog")
public final class CatalogMenu {

  private static final List<CatalogProduct> PRODUCTS =
      List.of(
          new CatalogProduct("Diamond Pickaxe", "DIAMOND_PICKAXE", "Tools", 500),
          new CatalogProduct("Diamond Sword", "DIAMOND_SWORD", "Tools", 750),
          new CatalogProduct("Diamond Axe", "DIAMOND_AXE", "Tools", 600),
          new CatalogProduct("Bricks x64", "BRICKS", "Blocks", 200),
          new CatalogProduct("Stone x64", "STONE", "Blocks", 100),
          new CatalogProduct("Oak Planks x64", "OAK_PLANKS", "Blocks", 150),
          new CatalogProduct("Cooked Beef x16", "COOKED_BEEF", "Food", 300),
          new CatalogProduct("Bread x16", "BREAD", "Food", 200),
          new CatalogProduct("Golden Apple", "GOLDEN_APPLE", "Food", 1000));

  private static final List<String> CATEGORIES = List.of("Tools", "Blocks", "Food");

  @Reactive private final State<String> category = State.of("Tools");

  @Button(id = "back")
  public void back(MenuClick click) {
    click.open(MainMenu.class);
  }

  @Button(id = "next-category")
  public void nextCategory() {
    int idx = CATEGORIES.indexOf(category.get());
    category.set(CATEGORIES.get((idx + 1) % CATEGORIES.size()));
  }

  @Paginated
  public List<MenuItem> products() {
    return PRODUCTS.stream()
        .filter(p -> p.category().equals(category.get()))
        .map(this::toMenuItem)
        .toList();
  }

  private MenuItem toMenuItem(CatalogProduct product) {
    Icon icon =
        Icon.of(product.material())
            .named("<yellow>" + product.name() + "</yellow>")
            .describedBy(
                List.of(
                    "<gray>Category: <white>" + product.category() + "</white></gray>",
                    "<gray>Price: <gold>" + product.price() + " coins</gold></gray>"));
    return MenuItem.of(icon)
        .onClick(
            context ->
                MenuClick.of(context).message("<green>Selected " + product.name() + "</green>"));
  }
}
