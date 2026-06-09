package dev.haniel.menu.example.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.MenuFramework;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class MenuCommandServiceTest {

  private final MenuFramework framework = mock(MenuFramework.class);
  private final MenuCommandService service = new MenuCommandService(framework);

  @Test
  void rejectsNonPlayerSenders() {
    CommandSender console = mock(CommandSender.class);

    service.execute(console, new String[0]);

    verify(console).sendMessage(miniMessage("<red>Only players can use this command.</red>"));
    verifyNoInteractions(framework);
  }

  @Test
  void opensMainWhenNoArgsGiven() {
    Player player = mock(Player.class);

    service.execute(player, new String[0]);

    verify(framework).open(player, new MenuId("main"));
  }

  @Test
  void opensMainOnMainArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"main"});

    verify(framework).open(player, new MenuId("main"));
  }

  @Test
  void opensCatalogOnCatalogArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"catalog"});

    verify(framework).open(player, new MenuId("catalog"));
  }

  @Test
  void normalizesArgCasing() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"CATALOG"});

    verify(framework).open(player, new MenuId("catalog"));
  }

  @Test
  void showsUsageOnUnknownArg() {
    Player player = mock(Player.class);

    service.execute(player, new String[] {"unknown"});

    verify(player)
        .sendMessage(miniMessage("<yellow>Usage: /menuexample [main|catalog|reload]</yellow>"));
    verifyNoInteractions(framework);
  }

  private static Component miniMessage(String text) {
    return MiniMessage.miniMessage().deserialize(text);
  }
}
