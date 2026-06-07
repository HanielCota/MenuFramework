package dev.haniel.menu.example.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.paper.MenuFramework;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class MenuNavigatorTest {

  private final MenuMessages messages = mock(MenuMessages.class);
  private final MenuFramework framework = mock(MenuFramework.class);
  private final MenuNavigator navigator = new MenuNavigator(messages);

  @Test
  void deniesOpeningWithoutPermission() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.MAIN.permission())).thenReturn(false);
    navigator.attach(framework);

    navigator.openMain(player);

    verify(messages).send(player, "<red>You do not have permission to open this menu.</red>");
    verify(framework, never()).open(ArgumentMatchers.any(), ArgumentMatchers.<MenuId>any());
  }

  @Test
  void opensMainForPermittedPlayer() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.MAIN.permission())).thenReturn(true);
    navigator.attach(framework);

    navigator.openMain(player);

    verify(framework).open(player, ExampleMenu.MAIN.id());
  }

  @Test
  void opensCatalogForPermittedPlayer() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.CATALOG.permission())).thenReturn(true);
    navigator.attach(framework);

    navigator.openCatalog(player);

    verify(framework).open(player, ExampleMenu.CATALOG.id());
  }

  @Test
  void warnsWhenFrameworkNotAttached() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.MAIN.permission())).thenReturn(true);

    navigator.openMain(player);

    verify(messages).send(player, "<red>Menu framework is not ready.</red>");
  }
}
