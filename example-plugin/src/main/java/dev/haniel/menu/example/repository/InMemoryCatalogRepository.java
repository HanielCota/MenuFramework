package dev.haniel.menu.example.repository;

import dev.haniel.menu.example.domain.CatalogCategory;
import dev.haniel.menu.example.domain.CatalogProduct;
import dev.haniel.menu.example.domain.CatalogProducts;
import dev.haniel.menu.example.domain.MaterialName;
import dev.haniel.menu.example.domain.Price;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** In-memory catalog used by the example plugin. */
public final class InMemoryCatalogRepository implements CatalogRepository {

  private final CatalogProducts products;

  private InMemoryCatalogRepository(CatalogProducts products) {
    this.products = products;
  }

  public static InMemoryCatalogRepository create() {
    return new InMemoryCatalogRepository(new CatalogProducts(seedProducts()));
  }

  @Override
  public CatalogProducts products() {
    return products;
  }

  private static List<CatalogProduct> seedProducts() {
    return Stream.of(tools(), blocks(), food()).flatMap(List::stream).toList();
  }

  private static List<CatalogProduct> tools() {
    return IntStream.rangeClosed(1, 18)
        .mapToObj(
            index -> product("Tool " + index, "DIAMOND_PICKAXE", CatalogCategory.TOOLS, index))
        .toList();
  }

  private static List<CatalogProduct> blocks() {
    return IntStream.rangeClosed(1, 18)
        .mapToObj(index -> product("Block Pack " + index, "BRICKS", CatalogCategory.BLOCKS, index))
        .toList();
  }

  private static List<CatalogProduct> food() {
    return IntStream.rangeClosed(1, 18)
        .mapToObj(index -> product("Meal " + index, "COOKED_BEEF", CatalogCategory.FOOD, index))
        .toList();
  }

  private static CatalogProduct product(
      String name, String material, CatalogCategory category, int index) {
    return new CatalogProduct(
        name, new MaterialName(material), category, new Price(100 + index * 25));
  }
}
