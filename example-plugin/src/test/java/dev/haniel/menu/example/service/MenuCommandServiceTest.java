package dev.haniel.menu.example.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.paper.MenuFramework;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class MenuCommandServiceTest {

  private final MenuFramework framework = mock(MenuFramework.class);
  private final MenuReloader reloader = mock(MenuReloader.class);
  private final MenuMessages messages = mock(MenuMessages.class);
  private final MenuCommandService service = new MenuCommandService(framework, reloader, messages);

  @Test
  void rejectsNonPlayerSenders() {
    CommandSender console = mock(CommandSender.class);

    service.execute(console, new String[0]);

    verify(messages).send(console, "<red>Only players can use this command.</red>");
    verifyNoInteractions(framework, reloader);
  }

  @Test
  void opensMainWhenNoArgsGiven() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.MAIN.permission())).thenReturn(true);

    service.execute(player, new String[0]);

    verify(framework).open(player, ExampleMenu.MAIN.id());
  }

  @Test
  void opensMainOnMainArg() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.MAIN.permission())).thenReturn(true);

    service.execute(player, new String[] {"main"});

    verify(framework).open(player, ExampleMenu.MAIN.id());
  }

  @Test
  void opensCatalogOnCatalogArg() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.CATALOG.permission())).thenReturn(true);

    service.execute(player, new String[] {"catalog"});

    verify(framework).open(player, ExampleMenu.CATALOG.id());
  }

  @Test
  void reloadsOnReloadArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"reload"});

    verify(reloader).reloadAll(player);
  }

  @Test
  void normalizesArgCasing() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.CATALOG.permission())).thenReturn(true);

    service.execute(player, new String[] {"CATALOG"});

    verify(framework).open(player, ExampleMenu.CATALOG.id());
  }

  @Test
  void showsUsageOnUnknownArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"unknown"});

    verify(messages).send(player, "<yellow>Usage: /menuexample [main|catalog|reload]</yellow>");
    verifyNoInteractions(framework, reloader);
  }

  @Test
  void deniesMenuOpenWithoutPermission() {
    Player player = mock(Player.class);
    when(player.hasPermission(ExampleMenu.CATALOG.permission())).thenReturn(false);

    service.execute(player, new String[] {"catalog"});

    verify(messages).send(player, "<red>You do not have permission to open this menu.</red>");
    verify(framework, never()).open(player, ExampleMenu.CATALOG.id());
  }
}
