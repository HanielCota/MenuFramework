package dev.haniel.menu.example;

import dev.haniel.menu.example.command.MenuExampleCommand;
import dev.haniel.menu.example.domain.ExampleMenu;
import dev.haniel.menu.example.menu.CatalogMenu;
import dev.haniel.menu.example.menu.MainMenu;
import dev.haniel.menu.example.repository.CatalogRepository;
import dev.haniel.menu.example.repository.InMemoryCatalogRepository;
import dev.haniel.menu.example.service.MenuCommandService;
import dev.haniel.menu.example.service.MenuMessages;
import dev.haniel.menu.example.service.MenuReloader;
import dev.haniel.menu.paper.MenuFramework;
import java.util.Arrays;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boots the example plugin and wires the framework with explicit dependencies.
 *
 * <p>The bootstrap only saves bundled menu YAML, creates services, scans the example menu package
 * and registers the command. Menu behavior lives in menu classes, and command logic delegates to
 * {@link MenuCommandService}.
 */
public final class ExamplePlugin extends JavaPlugin {

  private MenuFramework framework;

  @Override
  public void onEnable() {
    saveDefaultMenus();

    CatalogRepository catalog = InMemoryCatalogRepository.create();
    MenuMessages messages = new MenuMessages();
    MenuReloader reloader = new MenuReloader(messages, getLogger(), this, () -> framework);

    MenuFramework menus =
        MenuFramework.builder(this)
            .instantiator(type -> menuInstance(type, catalog, reloader))
            .scan("dev.haniel.menu.example.menu")
            .build();

    framework = menus;
    bindCommand(new MenuCommandService(menus, reloader, messages));
  }

  @Override
  public void onDisable() {
    if (framework == null) {
      return;
    }
    framework.shutdown();
    framework = null;
  }

  private void saveDefaultMenus() {
    Arrays.stream(ExampleMenu.values())
        .map(ExampleMenu::resourcePath)
        .forEach(path -> saveResource(path, false));
  }

  private Object menuInstance(Class<?> type, CatalogRepository catalog, MenuReloader reloader) {
    if (type.equals(MainMenu.class)) {
      return new MainMenu(reloader);
    }
    if (type.equals(CatalogMenu.class)) {
      return new CatalogMenu(catalog);
    }
    throw new IllegalArgumentException("Unsupported example menu: " + type.getName());
  }

  private void bindCommand(MenuCommandService service) {
    PluginCommand command = getCommand("menuexample");
    if (command == null) {
      getLogger().warning("Command 'menuexample' is not registered in plugin.yml");
      return;
    }
    command.setExecutor(new MenuExampleCommand(service));
  }
}
