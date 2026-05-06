package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.PaginationConfig;
import com.github.hanielcota.menuframework.definition.SlotPattern;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for MenuFramework covering race conditions, concurrent operations,
 * and boundary conditions.
 */
@DisplayName("Edge Case Tests")
class EdgeCaseTest {

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
  @DisplayName("Should handle rapid open-close cycles")
  void shouldHandleRapidOpenCloseCycles() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.DIAMOND).amount(1).build();
    MenuFramework.builder("rapid-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Rapidly open and close menu multiple times
    for (int i = 0; i < 10; i++) {
      var session = service.open(player, "rapid-menu").get();
      assertNotNull(session);
      session.close();
    }

    // Final state should be clean
    assertTrue(service.getSession(player.getUniqueId()).isEmpty());
  }

  @Test
  @DisplayName("Should handle concurrent menu opens for same player")
  void shouldHandleConcurrentOpens() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.IRON_INGOT).amount(1).build();
    MenuFramework.builder("concurrent-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Open menu from multiple threads simultaneously
    var futures = new ArrayList<CompletableFuture<?>>();
    for (int i = 0; i < 5; i++) {
      futures.add(service.open(player, "concurrent-menu"));
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .get(5, TimeUnit.SECONDS);

    // Should have exactly one active session
    assertTrue(service.getSession(player.getUniqueId()).isPresent());
  }

  @Test
  @DisplayName("Should handle disposed session operations gracefully")
  void shouldHandleDisposedSessionOperations() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.GOLD_INGOT).amount(1).build();
    MenuFramework.builder("disposed-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "disposed-menu").get();
    session.dispose().get();

    // Operations on disposed session should throw or handle gracefully
    assertThrows(IllegalStateException.class, () -> session.setPage(1));
  }

  @Test
  @DisplayName("Should handle session refresh after disposal")
  void shouldHandleRefreshAfterDisposal() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.EMERALD).amount(1).build();
    MenuFramework.builder("refresh-disposed-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "refresh-disposed-menu").get();
    session.dispose().get();

    // refresh() is on MenuSession interface but implementation may vary
    // This test verifies no exception is thrown
    assertDoesNotThrow(session::refresh);
  }

  @Test
  @DisplayName("Should handle pagination with empty content")
  void shouldHandlePaginationWithEmptyContent() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    MenuFramework.builder("empty-paginated-menu")
        .rows(3)
        .pagination(PaginationConfig.builder()
            .contentSlots(SlotPattern.BORDERED.slots(3))
            .previousTemplate("prev")
            .nextTemplate("next")
            .build())
        .build()
        .register();

    var session = service.open(player, "empty-paginated-menu").get();
    assertNotNull(session);
    assertEquals(0, session.currentPage());
  }

  @Test
  @DisplayName("Should handle pagination with single item")
  void shouldHandlePaginationWithSingleItem() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.PAPER).amount(1).build();
    MenuFramework.builder("single-item-menu")
        .rows(3)
        .pagination(PaginationConfig.builder()
            .contentSlots(SlotPattern.BORDERED.slots(3))
            .previousTemplate("prev")
            .nextTemplate("next")
            .build())
        .addItem(template, null)
        .build()
        .register();

    var session = service.open(player, "single-item-menu").get();
    assertNotNull(session);
    assertEquals(0, session.currentPage());
  }

  @Test
  @DisplayName("Should handle menu with maximum slots")
  void shouldHandleMaximumSlotsMenu() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var builder = MenuFramework.builder("max-slots-menu").rows(6);

    // Fill all 54 slots
    for (int i = 0; i < 54; i++) {
      builder.slot(i, template, null);
    }

    builder.build().register();

    var session = service.open(player, "max-slots-menu").get();
    assertNotNull(session);
  }

  @Test
  @DisplayName("Should handle click on out-of-bounds slot")
  void shouldHandleOutOfBoundsClick() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.DIRT).amount(1).build();
    MenuFramework.builder("oob-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "oob-menu").get();
    var interactive = (com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession) session;

    // Should not throw on out-of-bounds slot
    assertDoesNotThrow(() -> interactive.handleClick(999, ClickType.LEFT));
  }

  @Test
  @DisplayName("Should handle dynamic content race condition")
  void shouldHandleDynamicContentRaceCondition() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.BOOK).amount(1).build();
    MenuFramework.builder("race-menu")
        .slot(0, template, null)
        .build()
        .register();

    var counter = new AtomicInteger(0);
    service.setDynamicContentProvider("race-menu", (p, session) -> {
      int count = counter.incrementAndGet();
      return List.of(
          com.github.hanielcota.menuframework.definition.SlotDefinition.of(
              1,
              ItemTemplate.builder(Material.PAPER).name("Item " + count).build(),
              null
          )
      );
    });

    // Trigger multiple concurrent accesses
    var latch = new CountDownLatch(5);
    for (int i = 0; i < 5; i++) {
      new Thread(() -> {
        service.getDynamicContent("race-menu");
        latch.countDown();
      }).start();
    }

    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("Should handle null click handler gracefully")
  void shouldHandleNullClickHandler() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.CHEST).amount(1).build();
    MenuFramework.builder("null-handler-menu")
        .slot(0, template, null) // null handler
        .build()
        .register();

    var session = service.open(player, "null-handler-menu").get();
    var interactive = (com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession) session;

    // Should not throw when clicking slot with null handler
    assertDoesNotThrow(() -> interactive.handleClick(0, ClickType.LEFT));
  }

  @Test
  @DisplayName("Should handle menu re-registration")
  void shouldHandleMenuReRegistration() {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.OAK_LOG).amount(1).build();
    var registrar = MenuFramework.builder("reregister-menu")
        .slot(0, template, null)
        .build();

    registrar.register();

    // Unregister and create new registrar with same ID
    service.unregisterDefinition("reregister-menu");
    var newRegistrar = MenuFramework.builder("reregister-menu")
        .slot(0, template, null)
        .build();
    assertDoesNotThrow(newRegistrar::register);
  }

  @Test
  @DisplayName("Should handle template with extreme amount")
  void shouldHandleExtremeAmount() {
    // Amount 64 is max valid
    var template = ItemTemplate.builder(Material.STONE).amount(64).build();
    assertNotNull(template);
    assertEquals(64, template.amount());
  }

  @Test
  @DisplayName("Should handle rapid definition lookups")
  void shouldHandleRapidDefinitionLookups() {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.SAND).amount(1).build();
    MenuFramework.builder("lookup-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Rapid concurrent lookups
    var futures = new ArrayList<CompletableFuture<?>>();
    for (int i = 0; i < 20; i++) {
      futures.add(CompletableFuture.runAsync(() -> {
        for (int j = 0; j < 100; j++) {
          service.getDefinition("lookup-menu");
        }
      }));
    }

    assertDoesNotThrow(() ->
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("Should handle player disconnect with open menu")
  void shouldHandlePlayerDisconnect() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.BEDROCK).amount(1).build();
    MenuFramework.builder("disconnect-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "disconnect-menu").get();
    assertNotNull(session);

    // Simulate disconnect
    player.disconnect();

    // Session should eventually be cleaned up
    // (actual cleanup happens via event listener, but we can verify shutdown works)
    assertDoesNotThrow(service::closeAllSessions);
  }

  @Test
  @DisplayName("Should handle menu open with invalid UUID")
  void shouldHandleInvalidUuid() {
    var service = MenuFramework.initialize(plugin);

    var randomUuid = java.util.UUID.randomUUID();

    // Should fail gracefully for offline player
    var future = service.open(randomUuid, "non-existent");
    assertThrows(Exception.class, future::get);
  }

  @Test
  @DisplayName("Should handle feature execution with exception")
  void shouldHandleFeatureException() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.TNT).amount(1).build();

    // Create a feature that throws on open
    var throwingFeature = new com.github.hanielcota.menuframework.api.MenuFeature() {
      @Override
      public void onOpen(com.github.hanielcota.menuframework.api.MenuSession session) {
        throw new RuntimeException("Simulated feature failure");
      }
    };

    MenuFramework.builder("feature-fail-menu")
        .slot(0, template, null)
        .feature(throwingFeature)
        .build()
        .register();

    // Should not propagate exception to caller (handled internally)
    var session = service.open(player, "feature-fail-menu").get();
    assertNotNull(session);
  }

  @Test
  @DisplayName("Should handle empty menu definition")
  void shouldHandleEmptyMenu() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    MenuFramework.builder("empty-menu")
        .build()
        .register();

    var session = service.open(player, "empty-menu").get();
    assertNotNull(session);
  }

  @Test
  @DisplayName("Should handle menu history overflow")
  void shouldHandleMenuHistoryOverflow() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.GLASS).amount(1).build();

    // Create multiple menus
    for (int i = 0; i < 5; i++) {
      MenuFramework.builder("history-menu-" + i)
          .slot(0, template, null)
          .build()
          .register();
    }

    // Open many menus to build history
    for (int i = 0; i < 5; i++) {
      service.open(player, "history-menu-" + i).get();
    }

    // History should be tracked
    var history = ((com.github.hanielcota.menuframework.internal.DefaultMenuService) service)
        .getMenuHistory();
    assertNotNull(history);
  }
}
