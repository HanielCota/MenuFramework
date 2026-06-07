package dev.haniel.menu.config;

import dev.haniel.menu.item.Icon;
import java.util.List;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * The appearance of a single button, read from YAML. Carries no behaviour.
 *
 * <p>{@code name} and each {@code lore} line are MiniMessage strings, deserialized by the
 * Paper layer. The defaults keep a malformed entry usable rather than failing the load.
 *
 * @param slot the slot index; must be zero or positive (range checked against rows on merge)
 * @param material the material name; defaults to {@code STONE} when blank
 * @param name the MiniMessage display name; defaults to empty
 * @param lore the MiniMessage lore lines; defaults to empty
 */
@ConfigSerializable
public record ButtonConfig(int slot, String material, String name, List<String> lore) {

  public ButtonConfig {
    if (slot < 0) {
      throw new IllegalArgumentException("slot must be >= 0");
    }
    material = (material == null || material.isBlank()) ? "STONE" : material;
    name = (name == null) ? "" : name;
    lore = (lore == null) ? List.of() : List.copyOf(lore);
  }

  /**
   * Returns the appearance of this button, dropping its slot.
   *
   * @return the icon built from this button's material, name and lore
   */
  public Icon icon() {
    return new Icon(material, name, lore);
  }
}
