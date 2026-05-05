package com.github.hanielcota.menuframework.definition;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SlotDefinition Tests")
class SlotDefinitionTest {

  @Test
  @DisplayName("Should create basic slot")
  void shouldCreateBasicSlot() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertEquals(0, slot.slot());
    assertEquals(template, slot.template());
    assertNull(slot.handler());
    assertFalse(slot.navigational());
  }

  @Test
  @DisplayName("Should create navigational slot")
  void shouldCreateNavigationalSlot() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.navigational(5, template, null);

    assertEquals(5, slot.slot());
    assertTrue(slot.navigational());
  }

  @Test
  @DisplayName("Should create slot with cooldown")
  void shouldCreateSlotWithCooldown() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.withCooldown(0, template, null, 20);

    assertEquals(20, slot.cooldownTicks());
  }

  @Test
  @DisplayName("Should create slot with permission")
  void shouldCreateSlotWithPermission() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var fallback = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var slot = SlotDefinition.withPermission(0, template, null, "test.perm", fallback);

    assertEquals("test.perm", slot.requiredPermission());
    assertEquals(fallback, slot.permissionFallbackTemplate());
  }

  @Test
  @DisplayName("Should create handler-only slot")
  void shouldCreateHandlerOnlySlot() {
    var slot = SlotDefinition.withHandler(0, ctx -> {});

    assertNull(slot.template());
    assertNotNull(slot.handler());
  }

  @Test
  @DisplayName("Should create toggle slot")
  void shouldCreateToggleSlot() {
    var enabled = ItemTemplate.builder(Material.STONE).amount(1).build();
    var disabled = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var slot = SlotDefinition.toggle(0, enabled, disabled, true, (ctx, state) -> {});

    assertTrue(slot.toggle());
    assertNotNull(slot.toggleStateKey());
    assertTrue(slot.toggleStateKey().isEnabled());
  }

  @Test
  @DisplayName("Should reject slot below minimum")
  void shouldRejectSlotBelowMinimum() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    assertThrows(IllegalArgumentException.class, () ->
        SlotDefinition.of(-2, template, null));
  }

  @Test
  @DisplayName("Should accept sentinel slot -1")
  void shouldAcceptSentinelSlot() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    assertDoesNotThrow(() -> SlotDefinition.of(-1, template, null));
  }

  @Test
  @DisplayName("Should reject negative cooldown")
  void shouldRejectNegativeCooldown() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    assertThrows(IllegalArgumentException.class, () ->
        new SlotDefinition(0, template, null, false, -1, null, null, false, null, null));
  }

  @Test
  @DisplayName("Should create disabled toggle slot")
  void shouldCreateDisabledToggleSlot() {
    var enabled = ItemTemplate.builder(Material.STONE).amount(1).build();
    var disabled = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var slot = SlotDefinition.toggle(0, enabled, disabled, false, (ctx, state) -> {});

    assertEquals(disabled, slot.template());
    assertFalse(slot.toggleStateKey().isEnabled());
  }
}
