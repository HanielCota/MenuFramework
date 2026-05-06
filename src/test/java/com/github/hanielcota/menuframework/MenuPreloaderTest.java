package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.PaginationConfig;
import com.github.hanielcota.menuframework.definition.SlotPattern;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for menu pre-loading functionality and edge cases.
 */
@DisplayName("MenuPreloader Tests")
class MenuPreloaderTest {

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
  @DisplayName("Should preload simple menu")
  void shouldPreloadSimpleMenu() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.DIAMOND).amount(1).build();
    MenuFramework.builder("simple-menu")
        .slot(0, template, null)
        .build()
        .register();

    var future = service.preloader().preload("simple-menu");
    future.get(2, TimeUnit.SECONDS);

    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }

  @Test
  @DisplayName("Should preload menu with pagination")
  void shouldPreloadPaginatedMenu() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.PAPER).amount(1).build();
    var builder = MenuFramework.builder("paginated-menu")
        .rows(6)
        .pagination(PaginationConfig.builder()
            .contentSlots(SlotPattern.BORDERED.slots(6))
            .previousTemplate("prev")
            .nextTemplate("next")
            .build());

    // Add many items to trigger pagination
    for (int i = 0; i < 50; i++) {
      builder.addItem(template, null);
    }

    builder.build().register();

    var future = service.preloader().preload("paginated-menu");
    future.get(2, TimeUnit.SECONDS);

    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }

  @Test
  @DisplayName("Should handle preload of non-existent menu gracefully")
  void shouldHandleNonExistentMenuPreload() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var future = service.preloader().preload("non-existent");
    future.get(2, TimeUnit.SECONDS);

    // Should complete without exception even if menu doesn't exist
    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }

  @Test
  @DisplayName("Should preload multiple menus")
  void shouldPreloadMultipleMenus() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    MenuFramework.builder("menu1").slot(0, template, null).build().register();
    MenuFramework.builder("menu2").slot(0, template, null).build().register();
    MenuFramework.builder("menu3").slot(0, template, null).build().register();

    var future = service.preloader().preloadAll("menu1", "menu2", "menu3");
    future.get(2, TimeUnit.SECONDS);

    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }

  @Test
  @DisplayName("Should invalidate preloaded content")
  void shouldInvalidatePreloadedContent() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.IRON_INGOT).amount(1).build();
    MenuFramework.builder("invalidate-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Preload first
    var future = service.preloader().preload("invalidate-menu");
    future.get(2, TimeUnit.SECONDS);

    // Then invalidate
    assertDoesNotThrow(() -> service.preloader().invalidate("invalidate-menu"));
  }

  @Test
  @DisplayName("Should handle concurrent preloads")
  void shouldHandleConcurrentPreloads() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.GOLD_INGOT).amount(1).build();
    MenuFramework.builder("concurrent-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Launch multiple preloads simultaneously
    var future1 = service.preloader().preload("concurrent-menu");
    var future2 = service.preloader().preload("concurrent-menu");
    var future3 = service.preloader().preload("concurrent-menu");

    future1.get(2, TimeUnit.SECONDS);
    future2.get(2, TimeUnit.SECONDS);
    future3.get(2, TimeUnit.SECONDS);

    assertTrue(future1.isDone());
    assertTrue(future2.isDone());
    assertTrue(future3.isDone());
  }

  @Test
  @DisplayName("Should reject null menuId")
  void shouldRejectNullMenuId() {
    var service = MenuFramework.initialize(plugin);

    assertThrows(NullPointerException.class, () ->
        service.preloader().preload(null));
  }

  @Test
  @DisplayName("Should reject null player in player-specific preload")
  void shouldRejectNullPlayer() {
    var service = MenuFramework.initialize(plugin);

    assertThrows(NullPointerException.class, () ->
        service.preloader().preload(null, "menu"));
  }

  @Test
  @DisplayName("Should preload with player-specific dynamic content")
  void shouldPreloadWithPlayer() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.EMERALD).amount(1).build();
    MenuFramework.builder("player-menu")
        .slot(0, template, null)
        .build()
        .register();

    var future = service.preloader().preload(player, "player-menu");
    future.get(2, TimeUnit.SECONDS);

    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
  }

  @Test
  @DisplayName("Should handle invalidateAll gracefully")
  void shouldHandleInvalidateAll() {
    var service = MenuFramework.initialize(plugin);

    assertDoesNotThrow(() -> service.preloader().invalidateAll());
  }

  @Test
  @DisplayName("Should track preload state")
  void shouldTrackPreloadState() throws Exception {
    var service = MenuFramework.initialize(plugin);

    var template = ItemTemplate.builder(Material.DIAMOND_BLOCK).amount(1).build();
    MenuFramework.builder("state-menu")
        .slot(0, template, null)
        .build()
        .register();

    var preloader = (com.github.hanielcota.menuframework.internal.DefaultMenuPreloader) service.preloader();

    // Before preload - state should be null
    assertNull(preloader.getState("state-menu"));

    // After preload - state should exist
    var future = service.preloader().preload("state-menu");
    future.get(2, TimeUnit.SECONDS);

    var state = preloader.getState("state-menu");
    assertNotNull(state);
    assertTrue(state.isCompleted());
    assertFalse(state.isFailed());
    assertEquals("state-menu", state.menuId());
    assertTrue(state.durationMs() >= 0);
  }

  @Test
  @DisplayName("Should handle dynamic content provider failure gracefully")
  void shouldHandleDynamicContentFailure() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.BOOK).amount(1).build();
    MenuFramework.builder("failing-menu")
        .slot(0, template, null)
        .build()
        .register();

    // Register a dynamic content provider that throws
    service.setDynamicContentProvider("failing-menu", (p, session) -> {
      throw new RuntimeException("Simulated provider failure");
    });

    var future = service.preloader().preload(player, "failing-menu");
    future.get(2, TimeUnit.SECONDS);

    // Should complete without propagating exception (handled internally)
    assertTrue(future.isDone());
  }
}
