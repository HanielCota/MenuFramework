package dev.haniel.menu.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ItemTraitsTest {

  @Test
  void noneIsAPlainSingleItem() {
    ItemTraits traits = ItemTraits.none();

    assertEquals(1, traits.amount());
    assertFalse(traits.glowing());
    assertFalse(traits.unbreakable());
    assertTrue(traits.customModelData().isEmpty());
    assertTrue(traits.flags().isEmpty());
  }

  @Test
  void withMethodsAccumulateAndStayImmutable() {
    ItemTraits traits =
        ItemTraits.none()
            .amount(16)
            .withGlow()
            .withUnbreakable()
            .modelData(7)
            .hiding(ItemFlag.HIDE_DYE);

    assertEquals(16, traits.amount());
    assertTrue(traits.glowing());
    assertTrue(traits.unbreakable());
    assertEquals(OptionalInt.of(7), traits.customModelData());
    assertEquals(java.util.Set.of(ItemFlag.HIDE_DYE), traits.flags());
    assertEquals(1, ItemTraits.none().amount());
  }

  @Test
  void rejectsAmountOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> ItemTraits.none().amount(0));
    assertThrows(IllegalArgumentException.class, () -> ItemTraits.none().amount(65));
  }

  @Test
  void withHeadIsPreservedThroughOtherTraitChanges() {
    HeadSkin skin = new HeadSkin.Texture("dGV4");

    ItemTraits traits = ItemTraits.none().withHead(skin).amount(5).withGlow();

    assertEquals(
        java.util.Optional.of(skin), traits.head(), "head must survive later with-changes");
    assertEquals(5, traits.amount());
    assertTrue(traits.glowing());
  }

  @Test
  void noneHasNoHead() {
    assertTrue(ItemTraits.none().head().isEmpty());
  }

  @Test
  void copiesFlagsDefensively() {
    java.util.Set<ItemFlag> flags = new java.util.HashSet<>();
    flags.add(ItemFlag.HIDE_ENCHANTS);
    ItemTraits traits =
        new ItemTraits(1, false, false, OptionalInt.empty(), flags, java.util.Optional.empty());

    flags.add(ItemFlag.HIDE_ATTRIBUTES);

    assertEquals(1, traits.flags().size());
  }
}
