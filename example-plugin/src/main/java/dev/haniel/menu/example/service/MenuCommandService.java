package dev.haniel.menu.example.service;

import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.paper.MenuFramework;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles command intent without depending on Bukkit command boilerplate. */
public final class MenuCommandService {

  private final MenuFramework framework;
  private final MenuReloader reloader;
  private final MenuMessages messages;

  public MenuCommandService(MenuFramework framework, MenuReloader reloader, MenuMessages messages) {
    this.framework = framework;
    this.reloader = reloader;
    this.messages = messages;
  }

  public void execute(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
      messages.send(sender, "<red>Only players can use this command.</red>");
      return;
    }
    execute(player, action(args));
  }

  private void execute(Player player, String action) {
    switch (action) {
      case "main" -> open(player, ExampleMenu.MAIN);
      case "catalog" -> open(player, ExampleMenu.CATALOG);
      case "reload" -> reloader.reloadAll(player);
      default ->
          messages.send(player, "<yellow>Usage: /menuexample [main|catalog|reload]</yellow>");
    }
  }

  private void open(Player player, ExampleMenu menu) {
    if (!player.hasPermission(menu.permission())) {
      messages.send(player, "<red>You do not have permission to open this menu.</red>");
      return;
    }
    framework.open(player, menu.id());
  }

  private String action(String[] args) {
    if (args.length == 0) {
      return "main";
    }
    return args[0].toLowerCase(Locale.ROOT);
  }
}
