package dev.haniel.menu.paper;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.PagedCompiler;
import dev.haniel.menu.compiler.StaticCompiler;
import dev.haniel.menu.compiler.reader.ClickArguments;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.config.MenuLoader;
import dev.haniel.menu.merge.PagedMerger;
import dev.haniel.menu.merge.StaticMerger;
import dev.haniel.menu.paper.anvil.AnvilPrompts;
import dev.haniel.menu.paper.api.MenuOpener;
import dev.haniel.menu.paper.argument.MenuClickArgumentResolver;
import dev.haniel.menu.paper.argument.PlayerArgumentResolver;
import dev.haniel.menu.paper.placeholder.PapiPlaceholders;
import dev.haniel.menu.paper.registry.MenuCatalog;
import dev.haniel.menu.paper.registry.MenuRegistry;
import dev.haniel.menu.paper.render.BukkitInventoryFactory;
import dev.haniel.menu.paper.render.ItemFactory;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.MenuRuntime;
import dev.haniel.menu.scheduler.MenuScheduler;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

final class MenuRegistryFactory {

  private final JavaPlugin plugin;
  private final Function<Class<?>, Object> instances;

  MenuRegistryFactory(JavaPlugin plugin, Function<Class<?>, Object> instances) {
    this.plugin = plugin;
    this.instances = instances;
  }

  MenuRegistry create(Path menusPath, MenuScheduler scheduler, AnvilPrompts prompts) {
    MiniMessage miniMessage = MiniMessage.miniMessage();
    ItemFactory icons = new ItemFactory(miniMessage);
    DeferredMenuOpener opener = new DeferredMenuOpener();
    MenuRegistry registry =
        new MenuRegistry(
            compiler(menusPath, icons, miniMessage, opener, prompts),
            menuFactory(icons, miniMessage, scheduler),
            new MenuCatalog(),
            instances,
            plugin.getLogger());
    opener.delegateTo(registry);
    return registry;
  }

  private MenuFactory menuFactory(
      ItemFactory icons, MiniMessage miniMessage, MenuScheduler scheduler) {
    return new MenuFactory(
        new MenuRuntime(
            plugin.getLogger(),
            icons,
            miniMessage,
            scheduler,
            new BukkitInventoryFactory(),
            new PapiPlaceholders(miniMessage)));
  }

  private MenuCompiler<ItemStack> compiler(
      Path menusPath,
      ItemFactory icons,
      MiniMessage miniMessage,
      MenuOpener opener,
      AnvilPrompts prompts) {
    MenuLoader loader = new MenuLoader(menusPath);
    ClickArguments clickArguments = clickArguments(miniMessage, opener, prompts);
    return new MenuCompiler<>(
        new StaticCompiler<>(new StaticReader(clickArguments), loader, new StaticMerger<>(icons)),
        new PagedCompiler<>(new PagedReader(clickArguments), loader, new PagedMerger<>(icons)));
  }

  private static ClickArguments clickArguments(
      MiniMessage miniMessage, MenuOpener opener, AnvilPrompts prompts) {
    return new ClickArguments(
        List.of(
            new PlayerArgumentResolver(),
            new MenuClickArgumentResolver(miniMessage, opener, prompts)));
  }
}
