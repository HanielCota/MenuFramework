package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for error handling and edge cases.
 */
@DisplayName("Error Handling Tests")
class ErrorHandlingTest {

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
  @DisplayName("Should throw when service not initialized")
  void shouldThrowWhenNotInitialized() {
    MenuFramework.shutdown();
    assertThrows(IllegalStateException.class, MenuFramework::service);
  }

  @Test
  @DisplayName("Should reject double initialization")
  void shouldRejectDoubleInitialization() {
    var service = MenuFramework.initialize(plugin);
    assertThrows(IllegalStateException.class, () ->
        MenuFramework.initialize(plugin));
  }

  @Test
  @DisplayName("Should handle offline player gracefully")
  void shouldHandleOfflinePlayer() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();
    player.disconnect();

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    MenuFramework.builder("offline-menu", service)
        .slot(0, template, null)
        .build()
        .register();

    var future = service.open(player, "offline-menu");
    assertTrue(future.isCompletedExceptionally() || future.get() != null);
  }

  @Test
  @DisplayName("Should handle non-existent menu")
  void shouldHandleNonExistentMenu() {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var future = service.open(player, "non-existent");
    assertThrows(Exception.class, future::get);
  }

  @Test
  @DisplayName("Should validate slot definition bounds")
  void shouldValidateSlotDefinitionBounds() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    assertThrows(IllegalArgumentException.class, () ->
        SlotDefinition.of(-2, template, null));

    assertDoesNotThrow(() ->
        SlotDefinition.of(-1, template, null)); // -1 is sentinel value
  }

  @Test
  @DisplayName("Should handle null template in slot")
  void shouldHandleNullTemplateInSlot() {
    var service = MenuFramework.initialize(plugin);
    var registrar = MenuFramework.builder("null-template-menu", service)
        .slot(0, null, null)
        .build();

    assertNotNull(registrar);
    assertTrue(registrar.definition().slots().isEmpty());
  }

  @Test
  @DisplayName("Should reject invalid refresh interval")
  void shouldRejectInvalidRefreshInterval() {
    assertThrows(IllegalArgumentException.class, () ->
        com.github.hanielcota.menuframework.api.MenuFeatures.refreshInterval(0));

    assertThrows(IllegalArgumentException.class, () ->
        com.github.hanielcota.menuframework.api.MenuFeatures.refreshInterval(-1));
  }

  @Test
  @DisplayName("Should handle shutdown gracefully")
  void shouldHandleShutdownGracefully() {
    MenuFramework.initialize(plugin);
    assertDoesNotThrow(() -> {
      MenuFramework.shutdown();
      MenuFramework.shutdown(); // Double shutdown should not throw
    });
  }

  @Test
  @DisplayName("Should maintain session isolation")
  void shouldMaintainSessionIsolation() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player1 = server.addPlayer();
    var player2 = server.addPlayer();

    var template = ItemTemplate.builder(Material.DIAMOND).amount(1).build();
    MenuFramework.builder("isolated-menu", service)
        .slot(0, template, null)
        .build()
        .register();

    var session1 = service.open(player1, "isolated-menu").get();
    var session2 = service.open(player2, "isolated-menu").get();

    assertNotEquals(session1.viewerId(), session2.viewerId());
    assertNotEquals(session1.view(), session2.view());
  }
}
