package dev.haniel.menu.example.service;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.MenuFramework;
import java.util.Locale;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MenuCommandService {

  private static final MiniMessage MINI = MiniMessage.miniMessage();

  private final MenuFramework framework;

  public MenuCommandService(MenuFramework framework) {
    this.framework = framework;
  }

  public void execute(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(MINI.deserialize("<red>Only players can use this command.</red>"));
      return;
    }
    String action = args.length == 0 ? "main" : args[0].toLowerCase(Locale.ROOT);
    switch (action) {
      case "main" -> framework.open(player, new MenuId("main"));
      case "catalog" -> framework.open(player, new MenuId("catalog"));
      case "reload" -> reload(player);
      default ->
          sender.sendMessage(
              MINI.deserialize("<yellow>Usage: /menuexample [main|catalog|reload]</yellow>"));
    }
  }

  private void reload(Player player) {
    if (!player.hasPermission("menuexample.reload")) {
      player.sendMessage(
          MINI.deserialize("<red>You do not have permission to reload menus.</red>"));
      return;
    }
    framework
        .reloadAllReportAsync()
        .thenAccept(
            report ->
                player.sendMessage(
                    MINI.deserialize(
                        report.successful()
                            ? "<green>Reloaded " + report.successCount() + " menu(s).</green>"
                            : "<red>Reloaded with "
                                + report.failures().size()
                                + " failure(s).</red>")));
  }
}
