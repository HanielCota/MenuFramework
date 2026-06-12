package dev.haniel.menu.paper;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import java.io.ByteArrayInputStream;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class BundledMenusTest {

  @Test
  void savesTheBundledResourceWithoutOverwriting() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getResource("menus/shop.yml")).thenReturn(new ByteArrayInputStream(new byte[0]));

    new BundledMenus(plugin).saveIfBundled(new MenuId("shop"));

    verify(plugin).saveResource("menus/shop.yml", false);
  }

  @Test
  void skipsMenusDefinedEntirelyInCode() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getResource("menus/code-only.yml")).thenReturn(null);

    new BundledMenus(plugin).saveIfBundled(new MenuId("code-only"));

    verify(plugin, never()).saveResource(anyString(), anyBoolean());
  }
}
