package dev.haniel.menu.paper;

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
import org.bukkit.plugin.java.JavaPlugin;

/** Boot-time configuration for {@link MenuFramework}. */
public final class MenuFrameworkBuilder {

  private final JavaPlugin plugin;
  private final List<String> basePackages = new ArrayList<>();
  private MenuInstanceFactory instances = new MenuInstantiator();
  private Path menusDirectory;
  private MenuScheduler scheduler;
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
   * Wires the framework, registers the listener once and scans configured packages.
   *
   * @return a ready framework facade
   */
  public MenuFramework build() {
    ensureFresh();
    MenuScheduler selectedScheduler = scheduler();
    MenuRegistry registry = registry(menusPath(), selectedScheduler);
    MenuScanner scanner = new MenuScanner(new ClassGraphMenuDiscovery(), instances);
    MenuFramework framework = framework(registry, scanner, selectedScheduler);
    scanConfigured(framework);
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
      MenuRegistry registry, MenuScanner scanner, MenuScheduler scheduler) {
    MenuListener listener = new MenuListener();
    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    return new MenuFramework(
        registry, scanner, new MenuLifecycle(plugin, listener, scheduler.global()));
  }

  private MenuRegistry registry(Path menusPath, MenuScheduler scheduler) {
    return new MenuRegistryFactory(plugin, instances).create(menusPath, scheduler);
  }

  private void scanConfigured(MenuFramework framework) {
    if (basePackages.isEmpty()) {
      return;
    }
    framework.scan(basePackages.toArray(String[]::new));
  }
}
