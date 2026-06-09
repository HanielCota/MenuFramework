package dev.haniel.menu.example.repository;

import dev.haniel.menu.example.domain.CatalogCategory;
import dev.haniel.menu.example.domain.CatalogProduct;
import java.util.List;

/** Provides the products shown by the catalog menu. */
public interface CatalogRepository {

  List<CatalogProduct> productsIn(CatalogCategory category);
}
