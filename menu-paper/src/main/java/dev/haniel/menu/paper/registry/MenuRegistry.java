package dev.haniel.menu.paper.registry;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.api.MenuOpener;
import dev.haniel.menu.paper.discovery.MenuInstantiator;
import dev.haniel.menu.paper.discovery.MenuScanner;
import dev.haniel.menu.paper.hook.HookDefinitions;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Compiles annotated menus at boot and opens or reloads them at runtime.
 *
 * <p>{@link #register(Object)} performs the only reflection and IO (through the compiler) and
 * builds the openable menu; {@link #open(Player, MenuId)} delegates to it; {@link #reload(MenuId)}
 * recompiles from the YAML and swaps the openable atomically.
 */
public final class MenuRegistry implements MenuOpener {

  private final MenuCompiler<ItemStack> compiler;
  private final MenuFactory factory;
  private final MenuCatalog catalog;
  private final Function<Class<?>, Object> instances;
  private final MenuReloader reloader;

  public MenuRegistry(MenuCompiler<ItemStack> compiler, MenuFactory factory, MenuCatalog catalog) {
    this(compiler, factory, catalog, new MenuInstantiator());
  }

  public MenuRegistry(
      MenuCompiler<ItemStack> compiler,
      MenuFactory factory,
      MenuCatalog catalog,
      Function<Class<?>, Object> instances) {
    this(compiler, factory, catalog, instances, Logger.getLogger(MenuRegistry.class.getName()));
  }

  public MenuRegistry(
      MenuCompiler<ItemStack> compiler,
      MenuFactory factory,
      MenuCatalog catalog,
      Function<Class<?>, Object> instances,
      Logger logger) {
    this.compiler = Objects.requireNonNull(compiler, "compiler");
    this.factory = Objects.requireNonNull(factory, "factory");
    this.catalog = Objects.requireNonNull(catalog, "catalog");
    this.instances = Objects.requireNonNull(instances, "instances");
    this.reloader =
        new MenuReloader(compiler, factory, catalog, Objects.requireNonNull(logger, "logger"));
  }

  /**
   * Compiles and registers the given annotated menu instance.
   *
   * @param menu an object whose class is annotated with {@code @Menu}; never null
   */
  public void register(Object menu) {
    CompiledMenu<ItemStack> compiled = compiler.compile(menu);
    catalog.put(
        compiled.id(),
        new RegisteredMenu(compiled.id(), menu, openable(menu.getClass(), compiled)));
  }

  /**
   * Compiles and registers the given annotated menu class using the configured instantiator.
   *
   * @param menuType an annotated menu class; never null
   */
  public void register(Class<?> menuType) {
    register(menuType, instances);
  }

  private void register(Class<?> menuType, Function<Class<?>, Object> instanceFactory) {
    CompiledMenu<ItemStack> compiled = compiler.compile(menuType, instanceFactory);
    catalog.put(
        compiled.id(),
        new RegisteredMenu(
            compiled.id(),
            menuType,
            () -> instanceFactory.apply(menuType),
            openable(menuType, compiled)));
  }

  /**
   * Discovers every {@code @Menu} class under the base packages and registers it.
   *
   * <p>Runs once at boot. Each class is instantiated via this registry's configured instantiation
   * strategy and compiled by the existing pipeline. All failures are aggregated and reported
   * together; any failure fails the boot.
   *
   * @param discovery the discovery implementation; never null
   * @param basePackages the packages to scan; never empty
   */
  public void registerAll(MenuDiscovery discovery, String... basePackages) {
    new MenuScanner(discovery, instances).scanTypes(Set.of(basePackages), this::register);
  }

  /**
   * Discovers every {@code @Menu} class and registers it with the given instantiation strategy.
   *
   * @param discovery the discovery implementation; never null
   * @param instances creates menu instances from discovered classes; never null
   * @param basePackages the packages to scan; never empty
   */
  public void registerAll(
      MenuDiscovery discovery, Function<Class<?>, Object> instances, String... basePackages) {
    new MenuScanner(discovery, instances)
        .scanTypes(Set.of(basePackages), type -> register(type, instances));
  }

  /**
   * Opens the menu with the given id for the player, if it is registered.
   *
   * @param player the viewer; never null
   * @param id the menu id to open; never null
   */
  @Override
  public void open(Player player, MenuId id) {
    open(player, id, null);
  }

  /**
   * Opens the menu with the given id for the player, passing it an open argument, if it is
   * registered.
   *
   * <p>The argument is injected into any {@code @Arg} field of a paginated menu before its first
   * render. See {@link dev.haniel.menu.annotation.Arg}.
   *
   * @param player the viewer; never null
   * @param id the menu id to open; never null
   * @param argument the open argument, or {@code null} for none
   */
  public void open(Player player, MenuId id, Object argument) {
    catalog
        .find(id)
        .filter(menu -> menu.mayOpen(player))
        .ifPresent(menu -> menu.current().open(player, argument));
  }

  /**
   * Opens the menu registered from the given class for the player, if it is registered.
   *
   * @param player the viewer; never null
   * @param sourceType the annotated menu class; never null
   */
  @Override
  public void open(Player player, Class<?> sourceType) {
    open(player, sourceType, null);
  }

  /**
   * Opens the menu registered from the given class for the player, passing it an open argument, if
   * it is registered.
   *
   * @param player the viewer; never null
   * @param sourceType the annotated menu class; never null
   * @param argument the open argument, or {@code null} for none
   */
  public void open(Player player, Class<?> sourceType, Object argument) {
    catalog
        .find(sourceType)
        .filter(menu -> menu.mayOpen(player))
        .ifPresent(menu -> menu.current().open(player, argument));
  }

  /**
   * Recompiles the menu from its YAML and swaps in the new openable.
   *
   * @param id the menu id to reload; never null
   * @return {@code true} if the menu was registered and reloaded; {@code false} if it is not
   *     registered or its reload failed (the cause is logged). Use {@link #reloadReport(MenuId)} to
   *     tell the two apart programmatically.
   */
  public boolean reload(MenuId id) {
    return reloader.reload(id);
  }

  /**
   * Recompiles one menu and reports the outcome.
   *
   * @param id the menu id to reload; never null
   * @return the reload report
   */
  public ReloadReport reloadReport(MenuId id) {
    return reloader.reloadReport(id);
  }

  /**
   * Returns how many menus are registered.
   *
   * @return the menu count
   */
  public int size() {
    return catalog.size();
  }

  /** Removes every registered menu, freeing references for garbage collection. */
  public void clear() {
    catalog.clear();
  }

  /**
   * Recompiles every registered menu.
   *
   * @return the number of reloaded menus
   */
  public int reloadAll() {
    return reloader.reloadAllReport().successCount();
  }

  /**
   * Recompiles every registered menu and reports per-menu failures.
   *
   * @return the reload report
   */
  public ReloadReport reloadAllReport() {
    return reloader.reloadAllReport();
  }

  /**
   * Loads all YAML files asynchronously and applies the compiled menus on the provided sync
   * executor.
   *
   * @param asyncExecutor executor for file IO and YAML parsing
   * @param syncExecutor executor that owns Bukkit inventory/item APIs
   * @return a future completed with the reload report
   */
  public CompletableFuture<ReloadReport> reloadAllReportAsync(
      Executor asyncExecutor, Executor syncExecutor) {
    return reloader.reloadAllReportAsync(asyncExecutor, syncExecutor);
  }

  private PaperMenu openable(Class<?> sourceType, CompiledMenu<ItemStack> compiled) {
    if (compiled instanceof CompiledPagedMenu<?>) {
      HookDefinitions.of(sourceType);
    }
    return factory.create(compiled);
  }
}
