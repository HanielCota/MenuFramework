package dev.haniel.menu.paper;

import dev.haniel.menu.domain.MenuId;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Copies a menu's bundled {@code menus/<id>.yml} resource into the plugin data folder on first
 * boot.
 *
 * <p>Removes the boilerplate of listing every menu file and calling {@code saveResource} by hand:
 * the framework already knows each registered menu's id, so it can extract the matching default
 * YAML shipped inside the plugin jar. Saving is a no-op when the file already exists on disk, so
 * player edits are never overwritten, and when the jar carries no such resource (a menu defined
 * entirely in code).
 */
final class BundledMenus {

  private final JavaPlugin plugin;

  BundledMenus(JavaPlugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  /**
   * Extracts {@code menus/<id>.yml} from the jar into the data folder if it is bundled and absent.
   *
   * @param id the menu id whose default YAML should be saved; never null
   */
  void saveIfBundled(MenuId id) {
    String resourcePath = "menus/" + id.value() + ".yml";
    if (plugin.getResource(resourcePath) == null) {
      return;
    }
    plugin.saveResource(resourcePath, false);
  }
}
