package dev.haniel.menu.paper.placeholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PapiPlaceholdersTest {

  private final PapiPlaceholders placeholders = new PapiPlaceholders();
  private final PlayerId player = new PlayerId(UUID.randomUUID());

  @Test
  void returnsTextWithoutAPlaceholderUnchanged() {
    assertEquals("no tokens here", placeholders.resolve(player, "no tokens here"));
  }

  @Test
  void returnsTextUnchangedWhenPlaceholderApiIsAbsent() {
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      PluginManager pluginManager = mock(PluginManager.class);
      bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);
      when(pluginManager.isPluginEnabled("PlaceholderAPI")).thenReturn(false);

      assertEquals("%player_name%", placeholders.resolve(player, "%player_name%"));
    }
  }
}
