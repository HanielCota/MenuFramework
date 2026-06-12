package dev.haniel.menu.example;

import dev.haniel.menu.example.command.MenuExampleCommand;
import dev.haniel.menu.example.service.MenuCommandService;
import dev.haniel.menu.paper.MenuFramework;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

public final class ExamplePlugin extends JavaPlugin {

  private @Nullable MenuFramework framework;

  @Override
  public void onEnable() {
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
