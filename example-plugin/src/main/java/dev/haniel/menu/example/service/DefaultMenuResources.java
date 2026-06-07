package dev.haniel.menu.example.service;

import dev.haniel.menu.example.domain.ExampleMenu;
import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;

/** Extracts bundled menu YAML files into the plugin data folder. */
public final class DefaultMenuResources {

  private final JavaPlugin plugin;

  public DefaultMenuResources(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void saveAll() {
    Arrays.stream(ExampleMenu.values()).map(ExampleMenu::resourcePath).forEach(this::save);
  }

  private void save(String resourcePath) {
    plugin.saveResource(resourcePath, false);
  }
}
