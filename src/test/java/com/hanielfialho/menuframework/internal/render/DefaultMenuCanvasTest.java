package com.hanielfialho.menuframework.internal.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.MenuLayout;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

final class DefaultMenuCanvasTest {

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void backgroundFillsOnlyUnassignedSlots() {
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    canvas.background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    canvas.item(0, new ItemStack(Material.DIAMOND));
    canvas.empty(1);

    MenuFrame<String> frame = canvas.build();

    assertEquals(Material.DIAMOND, frame.slot(0).orElseThrow().icon().getType());
    assertFalse(frame.occupied(1));
    assertEquals(Material.GRAY_STAINED_GLASS_PANE, frame.slot(2).orElseThrow().icon().getType());
  }

  @Test
  void rejectsDuplicateSlotAssignment() {
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    canvas.item(3, new ItemStack(Material.STONE));

    assertThrows(IllegalStateException.class, () -> canvas.empty(3));
  }

  @Test
  void cannotBeMutatedOrBuiltTwiceAfterBuild() {
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    canvas.build();

    assertThrows(IllegalStateException.class, () -> canvas.item(0, new ItemStack(Material.STONE)));

    assertThrows(IllegalStateException.class, canvas::build);
  }

  @Test
  void itemStackIsCopiedOnInsertionAndAccess() {
    ItemStack source = new ItemStack(Material.DIAMOND, 3);
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    canvas.item(0, source);
    source.setAmount(12);

    MenuSlot<String> slot = canvas.build().slot(0).orElseThrow();

    ItemStack firstRead = slot.icon();
    firstRead.setAmount(20);
    ItemStack secondRead = slot.icon();

    assertEquals(3, secondRead.getAmount());
    assertNotSame(firstRead, secondRead);
  }

  @Test
  void buttonRetainsHandlerWithoutMakingPlainItemClickable() {
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    canvas.item(0, new ItemStack(Material.STONE));
    canvas.button(1, new ItemStack(Material.EMERALD), interaction -> {});

    MenuFrame<String> frame = canvas.build();

    assertFalse(frame.slot(0).orElseThrow().clickable());
    assertTrue(frame.slot(1).orElseThrow().clickable());
    assertTrue(frame.slot(1).orElseThrow().clickHandler().isPresent());
  }

  @Test
  void rejectsAirIconsAndDuplicateBackground() {
    DefaultMenuCanvas<String> canvas = new DefaultMenuCanvas<>(MenuLayout.chest(1));

    assertThrows(IllegalArgumentException.class, () -> canvas.item(0, new ItemStack(Material.AIR)));

    canvas.background(new ItemStack(Material.STONE));

    assertThrows(
        IllegalStateException.class, () -> canvas.background(new ItemStack(Material.DIAMOND)));
  }
}
