package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests using MockBukkit for the MenuFramework library.
 */
@DisplayName("MenuFramework Integration Tests")
class MenuFrameworkTest {

  private ServerMock server;
  private MenuTestPlugin plugin;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    plugin = MockBukkit.load(MenuTestPlugin.class);
  }

  @AfterEach
  void tearDown() {
    MenuFramework.shutdown();
    MockBukkit.unmock();
  }

  @Test
  @DisplayName("Should initialize framework and create service")
  void shouldInitializeFramework() {
    var service = MenuFramework.initialize(plugin);

    assertNotNull(service);
    assertEquals(service, MenuFramework.service());
  }

  @Test
  @DisplayName("Should create standalone service without singleton")
  void shouldCreateStandaloneService() {
    var service = MenuFramework.create(plugin);

    assertNotNull(service);
    assertThrows(IllegalStateException.class, MenuFramework::service);
  }

  @Test
  @DisplayName("Should register and open menu for player")
  void shouldRegisterAndOpenMenu() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.DIAMOND)
        .name("<green>Test Item")
        .amount(1)
        .build();

    MenuFramework.builder("test-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "test-menu").get();

    assertNotNull(session);
    assertEquals("test-menu", session.menuId());
    assertEquals(player.getUniqueId(), session.viewerId());
    assertTrue(session.isSameView(session.view()));
  }

  @Test
  @DisplayName("Should reject invalid amount in item template")
  void shouldRejectInvalidAmount() {
    assertThrows(IllegalArgumentException.class, () ->
        ItemTemplate.builder(Material.DIAMOND).amount(0));

    assertThrows(IllegalArgumentException.class, () ->
        ItemTemplate.builder(Material.DIAMOND).amount(65));
  }

  @Test
  @DisplayName("Should validate config values")
  void shouldValidateConfigValues() {
    var config = new MenuFrameworkConfig();

    assertThrows(IllegalArgumentException.class, () ->
        config.sessionCacheMaxSize(0));

    assertThrows(IllegalArgumentException.class, () ->
        config.sessionCacheExpireMinutes(-1));
  }

  @Test
  @DisplayName("Should handle menu builder with layout")
  void shouldHandleMenuBuilderWithLayout() {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    var registrar = MenuFramework.builder("layout-menu")
        .layout(
            "XXXXXXXXX",
            "X       X",
            "XXXXXXXXX")
        .bind('X', template)
        .build();

    assertNotNull(registrar.definition());
    assertEquals("layout-menu", registrar.definition().id());
  }

  @Test
  @DisplayName("Should dispose session on close")
  void shouldDisposeSessionOnClose() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.IRON_INGOT).amount(1).build();

    MenuFramework.builder("dispose-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "dispose-menu").get();
    assertNotNull(session);

    session.close();

    // Session should be closed - verify by checking the player no longer has the menu open
    assertNull(service.getSession(player.getUniqueId()).orElse(null));
  }
}
