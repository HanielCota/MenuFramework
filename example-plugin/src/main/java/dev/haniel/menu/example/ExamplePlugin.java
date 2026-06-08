package dev.haniel.menu.example;

import dev.haniel.menu.example.command.MenuExampleCommand;
import dev.haniel.menu.example.menu.CatalogMenu;
import dev.haniel.menu.example.menu.MainMenu;
import dev.haniel.menu.example.repository.CatalogRepository;
import dev.haniel.menu.example.repository.InMemoryCatalogRepository;
import dev.haniel.menu.example.service.DefaultMenuResources;
import dev.haniel.menu.example.service.MenuCommandService;
import dev.haniel.menu.example.service.MenuMessages;
import dev.haniel.menu.example.service.MenuNavigator;
import dev.haniel.menu.example.service.MenuReloader;
import dev.haniel.menu.paper.MenuFramework;
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
    new DefaultMenuResources(this).saveAll();

    CatalogRepository catalog = InMemoryCatalogRepository.create();
    MenuMessages messages = new MenuMessages();
    MenuNavigator navigator = new MenuNavigator(messages);
    MenuReloader reloader = new MenuReloader(messages, getLogger(), this);

    MenuFramework menus =
        MenuFramework.builder(this)
            .instantiator(type -> menuInstance(type, catalog, navigator, reloader))
            .scan("dev.haniel.menu.example.menu")
            .build();

    navigator.attach(menus);
    reloader.attach(menus);
    framework = menus;
    bindCommand(new MenuCommandService(navigator, reloader, messages));
  }

  @Override
  public void onDisable() {
    if (framework == null) {
      return;
    }
    framework.shutdown();
    framework = null;
  }

  private Object menuInstance(
      Class<?> type, CatalogRepository catalog, MenuNavigator navigator, MenuReloader reloader) {
    if (type.equals(MainMenu.class)) {
      return new MainMenu(navigator, reloader);
    }
    if (type.equals(CatalogMenu.class)) {
      return new CatalogMenu(catalog, navigator);
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
