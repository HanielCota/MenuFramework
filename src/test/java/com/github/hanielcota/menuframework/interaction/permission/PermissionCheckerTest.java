package com.github.hanielcota.menuframework.interaction.permission;

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

@DisplayName("PermissionChecker Tests")
class PermissionCheckerTest {

  private ServerMock server;
  private PermissionChecker checker;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    MockBukkit.load(MenuTestPlugin.class);
    checker = new PermissionChecker();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  @DisplayName("Should allow slot without permission requirement")
  void shouldAllowWithoutPermission() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertTrue(checker.hasPermission(player, slot));
  }

  @Test
  @DisplayName("Should allow player with required permission")
  void shouldAllowWithPermission() {
    var player = server.addPlayer();
    player.addAttachment(MockBukkit.load(MenuTestPlugin.class), "test.permission", true);
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.withPermission(0, template, null, "test.permission", null);

    assertTrue(checker.hasPermission(player, slot));
  }

  @Test
  @DisplayName("Should deny player without required permission")
  void shouldDenyWithoutPermission() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.withPermission(0, template, null, "test.permission", null);

    assertFalse(checker.hasPermission(player, slot));
  }

  @Test
  @DisplayName("Should allow empty permission string")
  void shouldAllowEmptyPermission() {
    var player = server.addPlayer();
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.withPermission(0, template, null, "", null);

    assertTrue(checker.hasPermission(player, slot));
  }
}
