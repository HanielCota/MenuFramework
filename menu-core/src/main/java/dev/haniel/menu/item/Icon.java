package dev.haniel.menu.item;

import java.util.List;

/**
 * The appearance of a slot, independent of any platform item type.
 *
 * <p>Built either from YAML (a {@code ButtonConfig}) or in code for dynamic paginated content.
 * {@code name} and each {@code lore} line are MiniMessage strings, deserialized by the Paper
 * layer. The {@code with}-style methods return new instances; the value object stays immutable.
 *
 * <p><strong>Security:</strong> {@code name}/{@code lore} are parsed as trusted MiniMessage. When
 * interpolating player-controlled text into them, escape it first — otherwise a crafted value could
 * inject click/hover tags into the rendered item.
 *
 * @param material the material name; must be non-blank
 * @param name the MiniMessage display name; never null
 * @param lore the MiniMessage lore lines; never null
 */
public record Icon(String material, String name, List<String> lore) {

  public Icon {
    if (material == null || material.isBlank()) {
      throw new IllegalArgumentException("Icon material cannot be blank");
    }
    name = (name == null) ? "" : name;
    lore = (lore == null) ? List.of() : List.copyOf(lore);
  }

  /**
   * Creates an icon for the given material with no name or lore.
   *
   * @param material the material name; never blank
   * @return a new icon
   */
  public static Icon of(String material) {
    return new Icon(material, "", List.of());
  }

  /**
   * Returns a copy of this icon with the given display name.
   *
   * @param name the MiniMessage name
   * @return a new icon
   */
  public Icon named(String name) {
    return new Icon(material, name, lore);
  }

  /**
   * Returns a copy of this icon with the given lore lines.
   *
   * @param lines the MiniMessage lore lines
   * @return a new icon
   */
  public Icon describedBy(List<String> lines) {
    return new Icon(material, name, lines);
  }
}
