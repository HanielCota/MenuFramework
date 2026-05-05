package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for menu session lifecycle and state management.
 */
@DisplayName("MenuSession Tests")
class MenuSessionTest {

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
  @DisplayName("Should track current page correctly")
  void shouldTrackCurrentPage() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.BOOK).amount(1).build();

    MenuFramework.builder("page-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "page-menu").get();

    assertEquals(0, session.currentPage());

    session.setPage(0); // Same page - should not refresh
    assertEquals(0, session.currentPage());
  }

  @Test
  @DisplayName("Should reject negative page number")
  void shouldRejectNegativePage() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.PAPER).amount(1).build();

    MenuFramework.builder("reject-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "reject-menu").get();

    assertThrows(IllegalArgumentException.class, () -> session.setPage(-1));
  }

  @Test
  @DisplayName("Should identify same view correctly")
  void shouldIdentifySameView() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.CHEST).amount(1).build();

    MenuFramework.builder("view-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "view-menu").get();

    assertTrue(session.isSameView(session.view()));
    assertFalse(session.isSameView(player.getOpenInventory()));
  }

  @Test
  @DisplayName("Should handle click on empty slot")
  void shouldHandleClickOnEmptySlot() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    MenuFramework.builder("empty-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "empty-menu").get();

    // Click on empty slot (slot 1) should not throw
    var interactive = (com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession) session;
    assertDoesNotThrow(() -> interactive.handleClick(1, ClickType.LEFT));
  }

  @Test
  @DisplayName("Should dispose session asynchronously")
  void shouldDisposeAsync() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.DIRT).amount(1).build();

    MenuFramework.builder("dispose-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "dispose-menu").get();

    var future = session.dispose();
    future.get(); // Should complete without exception

    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }
}
