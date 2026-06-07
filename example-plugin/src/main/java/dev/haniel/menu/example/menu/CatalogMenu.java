package dev.haniel.menu.example.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.example.domain.CatalogCategory;
import dev.haniel.menu.example.domain.CatalogProduct;
import dev.haniel.menu.example.repository.CatalogRepository;
import dev.haniel.menu.example.service.MenuNavigator;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.state.State;
import java.util.List;

/** Reactive paginated menu whose content comes from the example catalog repository. */
@Menu(id = "catalog")
public final class CatalogMenu {

  private final CatalogRepository catalog;
  private final MenuNavigator navigator;
  @Reactive private final State<CatalogCategory> category = State.of(CatalogCategory.TOOLS);

  public CatalogMenu(CatalogRepository catalog, MenuNavigator navigator) {
    this.catalog = catalog;
    this.navigator = navigator;
  }

  @Button(id = "back")
  public void back(MenuClick click) {
    navigator.openMain(click.player());
  }

  @Button(id = "next-category")
  public void nextCategory() {
    category.set(category.get().next());
  }

  @Paginated
  public List<MenuItem> products() {
    return catalog.products().in(category.get()).stream().map(this::item).toList();
  }

  private MenuItem item(CatalogProduct product) {
    Icon icon =
        Icon.of(product.material().value())
            .named("<yellow>" + product.name() + "</yellow>")
            .describedBy(List.of(loreCategory(product), lorePrice(product)));
    return MenuItem.of(icon).onClick(context -> select(context, product));
  }

  private String loreCategory(CatalogProduct product) {
    return "<gray>Category: <white>" + product.category().label() + "</white></gray>";
  }

  private String lorePrice(CatalogProduct product) {
    return "<gray>Price: <gold>" + product.price().formatted() + "</gold></gray>";
  }

  private void select(ClickContext context, CatalogProduct product) {
    MenuClick.of(context).message("<green>Selected " + product.name() + ".</green>");
  }
}
