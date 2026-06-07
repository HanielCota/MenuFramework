package dev.haniel.menu.example.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class MenuCommandServiceTest {

  private final MenuNavigator navigator = mock(MenuNavigator.class);
  private final MenuReloader reloader = mock(MenuReloader.class);
  private final MenuMessages messages = mock(MenuMessages.class);
  private final MenuCommandService service = new MenuCommandService(navigator, reloader, messages);

  @Test
  void rejectsNonPlayerSenders() {
    CommandSender console = mock(CommandSender.class);

    service.execute(console, new String[0]);

    verify(messages).send(console, "<red>Only players can use this command.</red>");
    verifyNoInteractions(navigator, reloader);
  }

  @Test
  void opensMainWhenNoArgsGiven() {
    Player player = mock(Player.class);

    service.execute(player, new String[0]);

    verify(navigator).openMain(player);
  }

  @Test
  void opensMainOnMainArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"main"});

    verify(navigator).openMain(player);
  }

  @Test
  void opensCatalogOnCatalogArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"catalog"});

    verify(navigator).openCatalog(player);
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

    service.execute(player, new String[] {"CATALOG"});

    verify(navigator).openCatalog(player);
  }

  @Test
  void showsUsageOnUnknownArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"unknown"});

    verify(messages).send(player, "<yellow>Usage: /menuexample [main|catalog|reload]</yellow>");
    verifyNoInteractions(navigator, reloader);
  }
}
