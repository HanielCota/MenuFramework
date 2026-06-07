package dev.haniel.menu.example.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A Bukkit material key kept as a domain value object instead of leaking Bukkit types into domain
 * data.
 *
 * @param value uppercase Bukkit material name
 */
public record MaterialName(String value) {

  private static final Pattern MATERIAL_KEY = Pattern.compile("[A-Z0-9_]+");

  public MaterialName {
    Objects.requireNonNull(value, "value");
    if (!MATERIAL_KEY.matcher(value).matches()) {
      throw new IllegalArgumentException("MaterialName must be an uppercase Bukkit material key");
    }
  }
}
