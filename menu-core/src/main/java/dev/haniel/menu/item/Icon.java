package dev.haniel.menu.item;

import java.util.List;

/**
 * The appearance of a slot, independent of any platform item type.
 *
 * <p>Built either from YAML (a {@code ButtonConfig}) or in code for dynamic paginated content.
 * {@code name} and each {@code lore} line are MiniMessage strings, deserialized by the Paper layer.
 * The {@link #traits()} carry the non-textual look (stack size, glint, model data, hidden tooltip
 * parts). The {@code with}-style methods return new instances; the value object stays immutable.
 *
 * <p><strong>Security:</strong> {@code name}/{@code lore} are parsed as trusted MiniMessage. When
 * interpolating player-controlled text into them, escape it first — otherwise a crafted value could
 * inject click/hover tags into the rendered item.
 *
 * @param material the material name; must be non-blank
 * @param name the MiniMessage display name; never null
 * @param lore the MiniMessage lore lines; never null
 * @param traits the non-textual appearance; never null
 */
public record Icon(String material, String name, List<String> lore, ItemTraits traits) {

  public Icon {
    if (material == null || material.isBlank()) {
      throw new IllegalArgumentException("Icon material cannot be blank");
    }
    name = (name == null) ? "" : name;
    lore = (lore == null) ? List.of() : List.copyOf(lore);
    traits = (traits == null) ? ItemTraits.none() : traits;
  }

  /**
   * Creates an icon with default traits.
   *
   * @param material the material name; never blank
   * @param name the MiniMessage display name
   * @param lore the MiniMessage lore lines
   */
  public Icon(String material, String name, List<String> lore) {
    this(material, name, lore, ItemTraits.none());
  }

  /**
   * Creates an icon for the given material with no name, lore or extra traits.
   *
   * @param material the material name; never blank
   * @return a new icon
   */
  public static Icon of(String material) {
    return new Icon(material, "", List.of(), ItemTraits.none());
  }

  /**
   * Returns a copy of this icon with the given display name.
   *
   * @param name the MiniMessage name
   * @return a new icon
   */
  public Icon named(String name) {
    return new Icon(material, name, lore, traits);
  }

  /**
   * Returns a copy of this icon with the given lore lines.
   *
   * @param lines the MiniMessage lore lines
   * @return a new icon
   */
  public Icon describedBy(List<String> lines) {
    return new Icon(material, name, lines, traits);
  }

  /**
   * Returns a copy of this icon with the given stack size.
   *
   * @param amount the stack size; must be in {@code 1..64}
   * @return a new icon
   */
  public Icon amount(int amount) {
    return new Icon(material, name, lore, traits.amount(amount));
  }

  /**
   * Returns a copy of this icon that shows the enchantment glint.
   *
   * @return a new icon
   */
  public Icon glowing() {
    return new Icon(material, name, lore, traits.withGlow());
  }

  /**
   * Returns a copy of this icon flagged unbreakable.
   *
   * @return a new icon
   */
  public Icon unbreakable() {
    return new Icon(material, name, lore, traits.withUnbreakable());
  }

  /**
   * Returns a copy of this icon with the given custom model data id.
   *
   * @param customModelData the model data id used by a resource pack
   * @return a new icon
   */
  public Icon modelData(int customModelData) {
    return new Icon(material, name, lore, traits.modelData(customModelData));
  }

  /**
   * Returns a copy of this icon hiding the given tooltip parts.
   *
   * @param flags the tooltip parts to hide
   * @return a new icon
   */
  public Icon hiding(ItemFlag... flags) {
    return new Icon(material, name, lore, traits.hiding(flags));
  }
}
