package com.github.hanielcota.menuframework.interaction.cooldown;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.hanielcota.menuframework.MenuTestPlugin;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CooldownManager Tests")
class CooldownManagerTest {

  private ServerMock server;
  private CooldownManager cooldownManager;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    MockBukkit.load(MenuTestPlugin.class);
    cooldownManager = new CooldownManager();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  @DisplayName("Should allow first click")
  void shouldAllowFirstClick() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertFalse(cooldownManager.isOnCooldown(player, slot));
  }

  @Test
  @DisplayName("Should block rapid clicks within global cooldown")
  void shouldBlockRapidClicks() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertFalse(cooldownManager.isOnCooldown(player, slot));
    assertTrue(cooldownManager.isOnCooldown(player, slot));
  }

  @Test
  @DisplayName("Should respect slot-specific cooldown")
  void shouldRespectSlotCooldown() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.withCooldown(0, template, null, 10); // 10 ticks = 500ms

    assertFalse(cooldownManager.isOnCooldown(player, slot));
    assertTrue(cooldownManager.isOnCooldown(player, slot));
  }

  @Test
  @DisplayName("Should allow clicks on different slots during global cooldown")
  void shouldAllowDifferentSlotsDuringGlobalCooldown() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot1 = SlotDefinition.of(0, template, null);
    var slot2 = SlotDefinition.of(1, template, null);

    assertFalse(cooldownManager.isOnCooldown(player, slot1));
    // During global cooldown, slot2 should also be blocked
    assertTrue(cooldownManager.isOnCooldown(player, slot2));
  }

  @Test
  @DisplayName("Should allow click after global cooldown expires")
  void shouldAllowAfterGlobalCooldownExpires() throws InterruptedException {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertFalse(cooldownManager.isOnCooldown(player, slot));
    Thread.sleep(150); // Global cooldown is 100ms
    assertFalse(cooldownManager.isOnCooldown(player, slot));
  }

  @Test
  @DisplayName("Should not block slots without cooldown")
  void shouldNotBlockSlotsWithoutCooldown() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertFalse(cooldownManager.isOnCooldown(player, slot));
    // Wait for global cooldown
    assertTrue(cooldownManager.isOnCooldown(player, slot));
  }
}
