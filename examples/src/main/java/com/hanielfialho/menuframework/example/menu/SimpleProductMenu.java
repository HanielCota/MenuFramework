package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.pagination.PaginationComponent;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** Exemplo simplificado de paginação síncrona usando o componente de alto nível. */
public final class SimpleProductMenu {

  private final Menu<PaginationComponent.State> menu;

  public SimpleProductMenu(List<Product> products) {
    this.menu =
        PaginationComponent.<Product>builder("Produtos", 6)
            .items(products)
            .entryRenderer(
                product ->
                    ItemStacks.named(
                        product.material(),
                        Component.text(product.name(), NamedTextColor.GREEN),
                        List.of(Component.text("Preço: " + product.price(), NamedTextColor.GOLD))))
            .onSelect(
                (product, interaction) -> {
                  Player viewer = interaction.viewer();
                  viewer.sendMessage(
                      Component.text("Selecionado: " + product.name(), NamedTextColor.GREEN));
                })
            .build();
  }

  public Menu<PaginationComponent.State> menu() {
    return this.menu;
  }
}
