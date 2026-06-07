package dev.haniel.menu.example.domain;

import java.util.List;

/** First-class collection for catalog products. */
public final class CatalogProducts {

  private final List<CatalogProduct> products;

  public CatalogProducts(List<CatalogProduct> products) {
    this.products = List.copyOf(products);
  }

  public List<CatalogProduct> in(CatalogCategory category) {
    return products.stream().filter(product -> product.matches(category)).toList();
  }
}
