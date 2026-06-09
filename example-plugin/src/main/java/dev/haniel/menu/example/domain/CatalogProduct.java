package dev.haniel.menu.example.domain;

import java.util.Objects;

/**
 * A product exposed by the example catalog.
 *
 * @param name display name used by the menu
 * @param material material key used by the menu renderer
 * @param category category that owns the product
 * @param price example price shown in lore
 */
public record CatalogProduct(
    String name, MaterialName material, CatalogCategory category, Price price) {

  public CatalogProduct {
    Objects.requireNonNull(name, "name");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Product name cannot be blank");
    }
    Objects.requireNonNull(material, "material");
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(price, "price");
  }
}
