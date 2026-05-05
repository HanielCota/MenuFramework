package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for menu builder functionality including layout binding and navigation.
 */
@DisplayName("MenuBuilder Tests")
class MenuBuilderTest {

  private ServerMock server;
  private MenuTestPlugin plugin;
  private MenuService service;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    plugin = MockBukkit.load(MenuTestPlugin.class);
    service = MenuFramework.initialize(plugin);
  }

  @AfterEach
  void tearDown() {
    MenuFramework.shutdown();
    MockBukkit.unmock();
  }

  @Test
  @DisplayName("Should build menu with layout")
  void shouldBuildMenuWithLayout() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    var registrar = MenuFramework.builder("layout-menu")
        .layout(
            "XXXXXXXXX",
            "X       X",
            "XXXXXXXXX")
        .bind('X', template)
        .build();

    assertNotNull(registrar);
    assertNotNull(registrar.definition());
    assertEquals("layout-menu", registrar.definition().id());
    assertFalse(registrar.definition().slots().isEmpty());
  }

  @Test
  @DisplayName("Should bind navigational slots")
  void shouldBindNavigationalSlots() {
    var template = ItemTemplate.builder(Material.ARROW).amount(1).build();

    var registrar = MenuFramework.builder("nav-menu")
        .layout("NNNNNNNNN")
        .bindNavigational('N', template)
        .build();

    assertNotNull(registrar);
    registrar.register();

    // Verify definition was registered correctly
    assertTrue(service.getDefinition("nav-menu").isPresent());
  }

  @Test
  @DisplayName("Should add dynamic content items")
  void shouldAddDynamicContent() {
    var template = ItemTemplate.builder(Material.BOOK).amount(1).build();

    var registrar = MenuFramework.builder("dynamic-menu")
        .slot(0, template, null)
        .addItem(ItemTemplate.builder(Material.PAPER).amount(1).build(), null)
        .build();

    assertNotNull(registrar);
    registrar.register();

    // Verify definition was registered correctly
    assertTrue(service.getDefinition("dynamic-menu").isPresent());
  }

  @Test
  @DisplayName("Should reject negative slot in builder")
  void shouldRejectNegativeSlot() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    assertThrows(IllegalArgumentException.class, () ->
        MenuFramework.builder("invalid-menu").slot(-1, template, null));
  }

  @Test
  @DisplayName("Should reject layout exceeding max rows")
  void shouldRejectExcessiveLayout() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    var builder = MenuFramework.builder("big-menu")
        .layout(
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX"); // 7 rows > max 6

    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  @DisplayName("Should handle layout with null rows")
  void shouldHandleLayoutWithNullRows() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    var registrar = MenuFramework.builder("null-row-menu")
        .layout("XXXXXXXXX", null, "XXXXXXXXX")
        .bind('X', template)
        .build();

    assertNotNull(registrar);
  }

  @Test
  @DisplayName("Should prevent double registration")
  void shouldPreventDoubleRegistration() {
    var template = ItemTemplate.builder(Material.DIRT).amount(1).build();

    var registrar = MenuFramework.builder("once-menu")
        .slot(0, template, null)
        .build();

    registrar.register();
    assertThrows(IllegalStateException.class, registrar::register);
  }
}
