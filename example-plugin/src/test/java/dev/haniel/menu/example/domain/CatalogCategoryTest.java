package dev.haniel.menu.example.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CatalogCategoryTest {

  @Test
  void exposesItsLabel() {
    assertEquals("Tools", CatalogCategory.TOOLS.label());
  }

  @Test
  void advancesToTheNextCategory() {
    assertEquals(CatalogCategory.BLOCKS, CatalogCategory.TOOLS.next());
  }

  @Test
  void wrapsAroundFromTheLastCategory() {
    assertEquals(CatalogCategory.TOOLS, CatalogCategory.FOOD.next());
  }
}
