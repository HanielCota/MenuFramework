package dev.haniel.menu.example.command;

import dev.haniel.menu.example.service.MenuCommandService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** Bukkit {@link CommandExecutor} that delegates to {@link MenuCommandService}. */
public final class MenuExampleCommand implements CommandExecutor {

  private final MenuCommandService service;

  public MenuExampleCommand(MenuCommandService service) {
    this.service = service;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      String @NotNull [] args) {
    service.execute(sender, args);
    return true;
  }
}
