package dev.haniel.menu.config;

import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.ItemFlag;
import dev.haniel.menu.item.ItemTraits;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * The appearance of a single button, read from YAML. Carries no behaviour.
 *
 * <p>{@code name} and each {@code lore} line are MiniMessage strings, deserialized by the Paper
 * layer. The defaults keep a malformed entry usable rather than failing the load.
 *
 * @param slot the slot index; must be zero or positive (range checked against rows on merge)
 * @param material the material name; defaults to {@code STONE} when blank
 * @param name the MiniMessage display name; defaults to empty
 * @param lore the MiniMessage lore lines; defaults to empty
 * @param amount the stack size; defaults to {@code 1} when not in {@code 1..64}
 * @param glow whether the item shows the enchantment glint; defaults to {@code false}
 * @param unbreakable whether the item is flagged unbreakable; defaults to {@code false}
 * @param modelData the custom model data id; {@code 0} or negative means none
 * @param flags the tooltip parts to hide; defaults to empty
 */
@ConfigSerializable
public record ButtonConfig(
    int slot,
    String material,
    String name,
    List<String> lore,
    int amount,
    boolean glow,
    boolean unbreakable,
    int modelData,
    List<ItemFlag> flags) {

  public ButtonConfig {
    if (slot < 0) {
      throw new IllegalArgumentException("slot must be >= 0");
    }
    material = (material == null || material.isBlank()) ? "STONE" : material;
    name = (name == null) ? "" : name;
    lore = (lore == null) ? List.of() : List.copyOf(lore);
    amount = (amount < 1 || amount > 64) ? 1 : amount;
    flags = (flags == null) ? List.of() : List.copyOf(flags);
  }

  /**
   * Creates a plain button with default traits (single item, no glint or hidden tooltip).
   *
   * @param slot the slot index; must be zero or positive
   * @param material the material name; defaults to {@code STONE} when blank
   * @param name the MiniMessage display name
   * @param lore the MiniMessage lore lines
   */
  public ButtonConfig(int slot, String material, String name, List<String> lore) {
    this(slot, material, name, lore, 1, false, false, 0, List.of());
  }

  /**
   * Returns the appearance of this button, dropping its slot.
   *
   * @return the icon built from this button's material, text and traits
   */
  public Icon icon() {
    return new Icon(material, name, lore, traits());
  }

  private ItemTraits traits() {
    OptionalInt model = modelData > 0 ? OptionalInt.of(modelData) : OptionalInt.empty();
    return new ItemTraits(amount, glow, unbreakable, model, Set.copyOf(flags), Optional.empty());
  }
}
