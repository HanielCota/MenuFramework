package dev.haniel.menu.example;

import dev.haniel.menu.example.command.MenuExampleCommand;
import dev.haniel.menu.example.service.MenuCommandService;
import dev.haniel.menu.paper.MenuFramework;
import java.util.List;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin {

  private static final List<String> MENU_FILES = List.of("menus/main.yml", "menus/catalog.yml");

  private MenuFramework framework;

  @Override
  public void onEnable() {
    MENU_FILES.forEach(path -> saveResource(path, false));

    framework = MenuFramework.builder(this).scan("dev.haniel.menu.example.menu").build();

    PluginCommand command = getCommand("menuexample");
    if (command != null) {
      command.setExecutor(new MenuExampleCommand(new MenuCommandService(framework)));
    }
  }

  @Override
  public void onDisable() {
    if (framework != null) {
      framework.shutdown();
      framework = null;
    }
  }
}
