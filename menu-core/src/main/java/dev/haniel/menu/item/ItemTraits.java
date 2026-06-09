package dev.haniel.menu.item;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The non-textual appearance of an {@link Icon}: everything beyond material, name and lore.
 *
 * <p>An immutable value object bundling stack size, glint, unbreakability, custom model data,
 * tooltip flags and any player-head skin so {@link Icon} stays small. The {@code with}-style
 * methods return new instances. Build from {@link #none()} and refine, e.g. {@code
 * ItemTraits.none().glowing().amount(3)}.
 *
 * @param amount the stack size shown on the item; must be in {@code 1..64}
 * @param glowing whether the item shows the enchantment glint; never null effect on tooltip
 * @param unbreakable whether the item is flagged unbreakable
 * @param customModelData the custom model data id, or empty for none; never null
 * @param flags the tooltip parts to hide; never null
 * @param head the player-head skin, or empty for a normal item; never null
 */
public record ItemTraits(
    int amount,
    boolean glowing,
    boolean unbreakable,
    OptionalInt customModelData,
    Set<ItemFlag> flags,
    Optional<HeadSkin> head) {

  private static final ItemTraits NONE =
      new ItemTraits(1, false, false, OptionalInt.empty(), Set.of(), Optional.empty());

  public ItemTraits {
    if (amount < 1 || amount > 64) {
      throw new IllegalArgumentException("amount must be between 1 and 64 but was " + amount);
    }
    customModelData = Objects.requireNonNull(customModelData, "customModelData");
    flags = (flags == null) ? Set.of() : Set.copyOf(flags);
    head = Objects.requireNonNull(head, "head");
  }

  /**
   * Returns the default traits: a single item with no glint, model data or hidden tooltip parts.
   *
   * @return the shared empty traits
   */
  public static ItemTraits none() {
    return NONE;
  }

  /**
   * Returns a copy with the given stack size.
   *
   * @param amount the stack size; must be in {@code 1..64}
   * @return new traits
   */
  public ItemTraits amount(int amount) {
    return new ItemTraits(amount, glowing, unbreakable, customModelData, flags, head);
  }

  /**
   * Returns a copy that shows the enchantment glint.
   *
   * @return new traits
   */
  public ItemTraits withGlow() {
    return new ItemTraits(amount, true, unbreakable, customModelData, flags, head);
  }

  /**
   * Returns a copy flagged unbreakable.
   *
   * @return new traits
   */
  public ItemTraits withUnbreakable() {
    return new ItemTraits(amount, glowing, true, customModelData, flags, head);
  }

  /**
   * Returns a copy with the given custom model data id.
   *
   * @param customModelData the model data id used by a resource pack
   * @return new traits
   */
  public ItemTraits modelData(int customModelData) {
    return new ItemTraits(
        amount, glowing, unbreakable, OptionalInt.of(customModelData), flags, head);
  }

  /**
   * Returns a copy hiding the given tooltip parts.
   *
   * @param flags the tooltip parts to hide
   * @return new traits
   */
  public ItemTraits hiding(ItemFlag... flags) {
    return new ItemTraits(amount, glowing, unbreakable, customModelData, Set.of(flags), head);
  }

  /**
   * Returns a copy showing the given player-head skin.
   *
   * @param head the head skin to show; never null
   * @return new traits
   */
  public ItemTraits withHead(HeadSkin head) {
    return new ItemTraits(amount, glowing, unbreakable, customModelData, flags, Optional.of(head));
  }
}
