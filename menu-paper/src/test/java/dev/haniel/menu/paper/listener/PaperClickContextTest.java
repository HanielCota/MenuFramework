package dev.haniel.menu.paper.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PaperClickContextTest {

  @Test
  void exposesPlayerAndClickType() {
    PlayerId id = new PlayerId(UUID.randomUUID());
    PaperClickContext context = new PaperClickContext(id, ClickType.SHIFT_LEFT);

    assertEquals(id, context.player());
    assertEquals(ClickType.SHIFT_LEFT, context.clickType());
  }

  @Test
  void resolvesPlayerEntityOnDemand() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      PaperClickContext context = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);
      assertSame(player, context.playerEntity());
    }
  }

  @Test
  void honoursTheClickContextContract() {
    PlayerId id = new PlayerId(UUID.randomUUID());
    ClickContext context = new PaperClickContext(id, ClickType.RIGHT);

    assertEquals(id, context.player());
    assertEquals(ClickType.RIGHT, context.clickType());
  }

  @Test
  void delegatesPermissionToTheOnlinePlayer() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    when(player.hasPermission("menu.use")).thenReturn(true);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      PaperClickContext context = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);

      assertTrue(context.hasPermission("menu.use"));
      assertFalse(context.hasPermission("menu.admin"));
    }
  }

  @Test
  void deniesPermissionWhenPlayerOffline() {
    UUID uuid = UUID.randomUUID();
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(null);
      PaperClickContext context = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);

      assertFalse(context.hasPermission("menu.use"));
    }
  }

  @Test
  void equalsByItsComponents() {
    UUID uuid = UUID.randomUUID();
    PaperClickContext first = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);
    PaperClickContext second = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }
}
