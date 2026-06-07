package dev.haniel.menu.example.service;

import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles command intent without depending on Bukkit command boilerplate. */
public final class MenuCommandService {

  private final MenuNavigator navigator;
  private final MenuReloader reloader;
  private final MenuMessages messages;

  public MenuCommandService(MenuNavigator navigator, MenuReloader reloader, MenuMessages messages) {
    this.navigator = navigator;
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
      case "main" -> navigator.openMain(player);
      case "catalog" -> navigator.openCatalog(player);
      case "reload" -> reloader.reloadAll(player);
      default ->
          messages.send(player, "<yellow>Usage: /menuexample [main|catalog|reload]</yellow>");
    }
  }

  private String action(String[] args) {
    if (args.length == 0) {
      return "main";
    }
    return args[0].toLowerCase(Locale.ROOT);
  }
}
