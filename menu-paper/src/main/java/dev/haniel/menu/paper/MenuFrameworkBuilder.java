package dev.haniel.menu.paper;

import dev.haniel.menu.paper.anvil.AnvilPromptListener;
import dev.haniel.menu.paper.anvil.AnvilPrompts;
import dev.haniel.menu.paper.api.MenuErrorHandler;
import dev.haniel.menu.paper.discovery.ClassGraphMenuDiscovery;
import dev.haniel.menu.paper.discovery.MenuInstanceFactory;
import dev.haniel.menu.paper.discovery.MenuInstantiator;
import dev.haniel.menu.paper.discovery.MenuScanner;
import dev.haniel.menu.paper.listener.MenuListener;
import dev.haniel.menu.paper.registry.MenuRegistry;
import dev.haniel.menu.scheduler.MenuScheduler;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

/** Boot-time configuration for {@link MenuFramework}. */
public final class MenuFrameworkBuilder {

  private final JavaPlugin plugin;
  private final List<String> basePackages = new ArrayList<>();
  private MenuInstanceFactory instances = new MenuInstantiator();
  private Path menusDirectory;
  private MenuScheduler scheduler;
  private MenuErrorHandler errorHandler;
  private boolean built;

  MenuFrameworkBuilder(JavaPlugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  /**
   * Adds packages that will be scanned during {@link #build()}.
   *
   * @param packages base packages to scan
   * @return this builder
   */
  public MenuFrameworkBuilder scan(String... packages) {
    PackageNames.requireValid(packages);
    basePackages.addAll(List.of(packages));
    return this;
  }

  /**
   * Uses a custom menu instantiation strategy for scanned classes.
   *
   * @param instances class-to-instance factory; never null
   * @return this builder
   */
  public MenuFrameworkBuilder instantiator(MenuInstanceFactory instances) {
    this.instances = Objects.requireNonNull(instances, "instances");
    return this;
  }

  /**
   * Overrides the menu YAML directory.
   *
   * @param menusDirectory directory holding {@code <menuId>.yml}; never null
   * @return this builder
   */
  public MenuFrameworkBuilder menusDirectory(Path menusDirectory) {
    this.menusDirectory = Objects.requireNonNull(menusDirectory, "menusDirectory");
    return this;
  }

  /**
   * Overrides platform scheduler detection.
   *
   * @param scheduler scheduler to use; never null
   * @return this builder
   */
  public MenuFrameworkBuilder scheduler(MenuScheduler scheduler) {
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    return this;
  }

  /**
   * Routes button actions that throw to the given handler instead of the default logging.
   *
   * @param errorHandler the handler invoked when an action throws; never null
   * @return this builder
   */
  public MenuFrameworkBuilder onActionError(MenuErrorHandler errorHandler) {
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    return this;
  }

  /**
   * Wires the framework, registers the listener once and scans configured packages.
   *
   * @return a ready framework facade
   */
  public MenuFramework build() {
    ensureFresh();
    MenuScheduler selectedScheduler = scheduler();
    AnvilPrompts prompts = new AnvilPrompts(MiniMessage.miniMessage());
    MenuRegistry registry = registry(menusPath(), selectedScheduler, prompts);
    MenuScanner scanner = new MenuScanner(new ClassGraphMenuDiscovery(), instances);
    scanConfigured(scanner, registry);
    MenuFramework framework = framework(registry, scanner, selectedScheduler, prompts);
    logRegistered(registry);
    return framework;
  }

  private void logRegistered(MenuRegistry registry) {
    plugin.getLogger().info("MenuFramework: " + registry.size() + " menu(s) registered");
  }

  private void ensureFresh() {
    if (built) {
      throw new IllegalStateException("MenuFrameworkBuilder can only build once");
    }
    built = true;
  }

  private Path menusPath() {
    return menusDirectory == null
        ? plugin.getDataFolder().toPath().resolve("menus")
        : menusDirectory;
  }

  private MenuScheduler scheduler() {
    MenuScheduler selected = scheduler == null ? SchedulerFactory.detect(plugin) : scheduler;
    plugin.getLogger().info("MenuFramework scheduler: " + selected.getClass().getSimpleName());
    return selected;
  }

  private MenuFramework framework(
      MenuRegistry registry, MenuScanner scanner, MenuScheduler scheduler, AnvilPrompts prompts) {
    MenuListener listener = listener();
    AnvilPromptListener anvilListener = new AnvilPromptListener(prompts);
    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    plugin.getServer().getPluginManager().registerEvents(anvilListener, plugin);
    MenuLifecycle lifecycle =
        new MenuLifecycle(plugin, List.of(listener, anvilListener), scheduler.global());
    return new MenuFramework(registry, scanner, lifecycle);
  }

  private MenuListener listener() {
    if (errorHandler == null) {
      return new MenuListener(plugin.getLogger());
    }
    return new MenuListener(errorHandler, plugin.getLogger());
  }

  private MenuRegistry registry(Path menusPath, MenuScheduler scheduler, AnvilPrompts prompts) {
    return new MenuRegistryFactory(plugin, instances).create(menusPath, scheduler, prompts);
  }

  private void scanConfigured(MenuScanner scanner, MenuRegistry registry) {
    if (basePackages.isEmpty()) {
      return;
    }
    scanner.scanTypes(Set.copyOf(basePackages), registry::register);
  }
}
