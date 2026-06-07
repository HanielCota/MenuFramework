package dev.haniel.menu.example.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogDomainTest {

  @Test
  void rejectsNegativePrices() {
    assertThrows(IllegalArgumentException.class, () -> new Price(-1));
  }

  @Test
  void rejectsInvalidMaterialNames() {
    assertThrows(IllegalArgumentException.class, () -> new MaterialName("stone"));
  }

  @Test
  void filtersProductsByCategory() {
    CatalogProduct tool =
        new CatalogProduct(
            "Tool", new MaterialName("DIAMOND_PICKAXE"), CatalogCategory.TOOLS, new Price(10));
    CatalogProduct food =
        new CatalogProduct(
            "Food", new MaterialName("COOKED_BEEF"), CatalogCategory.FOOD, new Price(5));

    CatalogProducts products = new CatalogProducts(List.of(tool, food));

    assertEquals(List.of(tool), products.in(CatalogCategory.TOOLS));
  }
}
