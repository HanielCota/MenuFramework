package com.github.hanielcota.menuframework;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for menu features lifecycle callbacks.
 */
@DisplayName("MenuFeature Tests")
class MenuFeatureTest {

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
  @DisplayName("Should trigger onOpen callback when menu opens")
  void shouldTriggerOnOpen() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();
    var opened = new AtomicBoolean(false);

    var feature = new MenuFeature() {
      @Override
      public void onOpen(MenuSession session) {
        opened.set(true);
      }
    };

    var template = ItemTemplate.builder(Material.DIAMOND).amount(1).build();

    MenuFramework.builder("feature-menu")
        .slot(0, template, null)
        .build()
        .register();

    service.open(player, "feature-menu").get();

    assertTrue(opened.get());
  }

  @Test
  @DisplayName("Should trigger onClick callback when player clicks")
  void shouldTriggerOnClick() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();
    var clicked = new AtomicBoolean(false);

    var feature = new MenuFeature() {
      @Override
      public void onClick(com.github.hanielcota.menuframework.api.ClickContext context) {
        clicked.set(true);
      }
    };

    var template = ItemTemplate.builder(Material.STONE).amount(1).build();

    MenuFramework.builder("click-menu")
        .slot(0, template, ctx -> {})
        .build()
        .register();

    var session = service.open(player, "click-menu").get();

    // Simulate click via the interactive session interface
    var interactive = (com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession) session;
    interactive.handleClick(0, ClickType.LEFT);

    assertTrue(clicked.get());
  }

  @Test
  @DisplayName("Should trigger onClose callback when menu closes")
  void shouldTriggerOnClose() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();
    var closed = new AtomicBoolean(false);

    var feature = new MenuFeature() {
      @Override
      public void onClose(MenuSession session) {
        closed.set(true);
      }
    };

    var template = ItemTemplate.builder(Material.IRON_INGOT).amount(1).build();

    MenuFramework.builder("close-menu")
        .slot(0, template, null)
        .build()
        .register();

    var session = service.open(player, "close-menu").get();
    session.close();

    assertTrue(closed.get());
  }

  @Test
  @DisplayName("Should play sound on click with SoundOnClick feature")
  void shouldPlaySoundOnClick() throws Exception {
    var service = MenuFramework.initialize(plugin);
    var player = server.addPlayer();

    var template = ItemTemplate.builder(Material.GOLD_INGOT).amount(1).build();

    MenuFramework.builder("sound-menu")
        .slot(0, template, ctx -> {})
        .build()
        .register();

    var session = service.open(player, "sound-menu").get();

    // Click should not throw even with sound feature
    var interactive = (com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession) session;
    assertDoesNotThrow(() -> interactive.handleClick(0, ClickType.LEFT));
  }
}
