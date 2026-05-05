package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.PaginationConfig;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for MenuDefinition defensive validations and construction.
 */
@DisplayName("MenuDefinition Validation Tests")
class MenuDefinitionTest {

  @Test
  @DisplayName("Should reject chest size not multiple of 9")
  void shouldRejectInvalidChestSize() {
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    assertThrows(IllegalArgumentException.class, () ->
        new MenuDefinition(
            "test", InventoryType.CHEST, 10, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should reject chest size below minimum")
  void shouldRejectChestSizeBelowMin() {
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    assertThrows(IllegalArgumentException.class, () ->
        new MenuDefinition(
            "test", InventoryType.CHEST, 8, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should reject chest size above maximum")
  void shouldRejectChestSizeAboveMax() {
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    assertThrows(IllegalArgumentException.class, () ->
        new MenuDefinition(
            "test", InventoryType.CHEST, 63, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should accept non-chest inventory types")
  void shouldAcceptNonChestTypes() {
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    var def = new MenuDefinition(
        "test", InventoryType.DROPPER, 9, net.kyori.adventure.text.Component.empty(),
        slots, null, PaginationConfig.builder().build(), List.of(), true, true, null);
    assertNotNull(def);
  }

  @Test
  @DisplayName("Should reject slot out of bounds")
  void shouldRejectSlotOutOfBounds() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    slots.put(27, SlotDefinition.of(27, template, null));

    assertThrows(IllegalArgumentException.class, () ->
        new MenuDefinition(
            "test", InventoryType.CHEST, 27, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should reject negative slot")
  void shouldRejectNegativeSlot() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    slots.put(-1, SlotDefinition.of(-1, template, null));

    assertThrows(IllegalArgumentException.class, () ->
        new MenuDefinition(
            "test", InventoryType.CHEST, 27, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should reject null id")
  void shouldRejectNullId() {
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    assertThrows(NullPointerException.class, () ->
        new MenuDefinition(
            null, InventoryType.CHEST, 27, net.kyori.adventure.text.Component.empty(),
            slots, null, PaginationConfig.builder().build(), List.of(), true, true, null));
  }

  @Test
  @DisplayName("Should accept valid definition")
  void shouldAcceptValidDefinition() {
    var template = ItemTemplate.builder(Material.DIAMOND).amount(1).build();
    var slots = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<SlotDefinition>();
    slots.put(0, SlotDefinition.of(0, template, null));

    var def = new MenuDefinition(
        "test", InventoryType.CHEST, 27, net.kyori.adventure.text.Component.empty(),
        slots, null, PaginationConfig.builder().build(), List.of(), true, true, null);

    assertEquals("test", def.id());
    assertEquals(InventoryType.CHEST, def.type());
    assertEquals(27, def.size());
  }
}
