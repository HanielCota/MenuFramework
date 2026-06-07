package dev.haniel.menu.example.domain;

/** Groups catalog products shown by the paginated example menu. */
public enum CatalogCategory {
  TOOLS("Tools"),
  BLOCKS("Blocks"),
  FOOD("Food");

  private final String label;

  CatalogCategory(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  public CatalogCategory next() {
    CatalogCategory[] categories = values();
    int nextIndex = (ordinal() + 1) % categories.length;
    return categories[nextIndex];
  }
}
