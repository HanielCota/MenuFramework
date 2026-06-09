package dev.haniel.menu.example.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
