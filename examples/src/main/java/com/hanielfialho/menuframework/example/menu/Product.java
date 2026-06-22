package com.hanielfialho.menuframework.example.menu;

import java.util.Objects;
import org.bukkit.Material;

/** Exemplo de objeto de domínio imutável exibido em um menu. */
public record Product(long id, Material material, String name, int price) {

  public Product {
    if (id <= 0L) {
      throw new IllegalArgumentException("id must be > 0: " + id);
    }

    Objects.requireNonNull(material, "material");
    Objects.requireNonNull(name, "name");

    if (material.isAir()) {
      throw new IllegalArgumentException("material cannot be AIR");
    }

    if (name.isBlank()) {
      throw new IllegalArgumentException("name cannot be blank");
    }

    if (price < 0) {
      throw new IllegalArgumentException("price must be >= 0: " + price);
    }
  }
}
