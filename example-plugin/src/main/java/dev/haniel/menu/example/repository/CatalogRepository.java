package dev.haniel.menu.example.repository;

import dev.haniel.menu.example.domain.CatalogProducts;

/** Provides the products shown by the catalog menu. */
public interface CatalogRepository {

  CatalogProducts products();
}
