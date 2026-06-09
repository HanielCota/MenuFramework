package dev.haniel.menu.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.example.domain.CatalogCategory;
import dev.haniel.menu.example.domain.CatalogProduct;
import dev.haniel.menu.example.domain.MaterialName;
import dev.haniel.menu.example.domain.Price;
import org.junit.jupiter.api.Test;

class InMemoryCatalogRepositoryTest {

  @Test
  void seedsEveryCategoryWithProducts() {
    CatalogRepository repository = InMemoryCatalogRepository.create();

    assertEquals(18, repository.productsIn(CatalogCategory.TOOLS).size());
    assertEquals(18, repository.productsIn(CatalogCategory.BLOCKS).size());
    assertEquals(18, repository.productsIn(CatalogCategory.FOOD).size());
  }

  @Test
  void seedsToolsWithTheirMaterialAndIndexedPrice() {
    CatalogRepository repository = InMemoryCatalogRepository.create();

    CatalogProduct firstTool = repository.productsIn(CatalogCategory.TOOLS).get(0);

    assertEquals("Tool 1", firstTool.name());
    assertEquals(new MaterialName("DIAMOND_PICKAXE"), firstTool.material());
    assertEquals(new Price(125), firstTool.price());
  }

  @Test
  void seedsEachCategoryWithItsOwnMaterial() {
    CatalogRepository repository = InMemoryCatalogRepository.create();

    CatalogProduct firstBlock = repository.productsIn(CatalogCategory.BLOCKS).get(0);
    CatalogProduct firstFood = repository.productsIn(CatalogCategory.FOOD).get(0);

    assertEquals(new MaterialName("BRICKS"), firstBlock.material());
    assertEquals(new MaterialName("COOKED_BEEF"), firstFood.material());
  }

  @Test
  void increasesSeededPriceWithTheProductIndex() {
    CatalogRepository repository = InMemoryCatalogRepository.create();

    CatalogProduct lastTool = repository.productsIn(CatalogCategory.TOOLS).get(17);

    assertEquals(new Price(550), lastTool.price());
  }
}
