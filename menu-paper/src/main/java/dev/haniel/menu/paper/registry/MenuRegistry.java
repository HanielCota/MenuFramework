package dev.haniel.menu.paper.registry;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.discovery.MenuDiscovery;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.api.MenuOpener;
import dev.haniel.menu.paper.discovery.MenuInstantiator;
import dev.haniel.menu.paper.discovery.MenuScanner;
import dev.haniel.menu.paper.hook.HookDefinitions;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;
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
  private final Logger logger;

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
    this.logger = Objects.requireNonNull(logger, "logger");
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
    catalog
        .find(id)
        .filter(menu -> menu.mayOpen(player))
        .ifPresent(menu -> menu.current().open(player));
  }

  /**
   * Opens the menu registered from the given class for the player, if it is registered.
   *
   * @param player the viewer; never null
   * @param sourceType the annotated menu class; never null
   */
  @Override
  public void open(Player player, Class<?> sourceType) {
    catalog
        .find(sourceType)
        .filter(menu -> menu.mayOpen(player))
        .ifPresent(menu -> menu.current().open(player));
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
    Optional<RegisteredMenu> found = catalog.find(id);
    if (found.isEmpty()) {
      logger.warning("Cannot reload unknown menu '" + id.value() + "'");
      return false;
    }
    return reloadOne(found.get()).successful();
  }

  /**
   * Recompiles one menu and reports the outcome.
   *
   * @param id the menu id to reload; never null
   * @return the reload report
   */
  public ReloadReport reloadReport(MenuId id) {
    return catalog.find(id).map(this::reloadOne).orElseGet(ReloadReport::empty);
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
    return reloadAllReport().successCount();
  }

  /**
   * Recompiles every registered menu and reports per-menu failures.
   *
   * @return the reload report
   */
  public ReloadReport reloadAllReport() {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    catalog.all().forEach(menu -> reloadInto(menu, reloaded, failures));
    return new ReloadReport(reloaded, failures);
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
    return CompletableFuture.supplyAsync(this::prepareAll, asyncExecutor)
        .thenApplyAsync(this::applyPrepared, syncExecutor);
  }

  private ReloadReport reloadOne(RegisteredMenu menu) {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    reloadInto(menu, reloaded, failures);
    return new ReloadReport(reloaded, failures);
  }

  private void reloadInto(
      RegisteredMenu menu, List<MenuId> reloaded, List<ReloadFailure> failures) {
    try {
      recompile(menu);
      reloaded.add(menu.id());
    } catch (RuntimeException exception) {
      logger.log(Level.WARNING, "Failed to reload menu '" + menu.id().value() + "'", exception);
      failures.add(ReloadFailure.from(menu.id(), exception));
    }
  }

  private PreparedReloads prepareAll() {
    List<PreparedReload> prepared = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    catalog.all().forEach(menu -> prepareInto(menu, prepared, failures));
    return new PreparedReloads(prepared, failures);
  }

  private void prepareInto(
      RegisteredMenu menu, List<PreparedReload> prepared, List<ReloadFailure> failures) {
    try {
      prepared.add(new PreparedReload(menu, compiler.load(menu.id())));
    } catch (RuntimeException exception) {
      logger.log(Level.WARNING, "Failed to load menu '" + menu.id().value() + "'", exception);
      failures.add(ReloadFailure.from(menu.id(), exception));
    }
  }

  private ReloadReport applyPrepared(PreparedReloads prepared) {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>(prepared.failures());
    prepared.reloads().forEach(reload -> applyInto(reload, reloaded, failures));
    return new ReloadReport(reloaded, failures);
  }

  private void applyInto(
      PreparedReload reload, List<MenuId> reloaded, List<ReloadFailure> failures) {
    try {
      RegisteredMenu menu = reload.menu();
      menu.swap(openable(menu.sourceType(), compile(menu, reload.config())));
      reloaded.add(menu.id());
    } catch (RuntimeException exception) {
      MenuId id = reload.menu().id();
      logger.log(Level.WARNING, "Failed to apply reloaded menu '" + id.value() + "'", exception);
      failures.add(ReloadFailure.from(id, exception));
    }
  }

  private RegisteredMenu recompile(RegisteredMenu menu) {
    menu.swap(openable(menu.sourceType(), compile(menu)));
    return menu;
  }

  private PaperMenu openable(Class<?> sourceType, CompiledMenu<ItemStack> compiled) {
    if (compiled instanceof CompiledPagedMenu<?>) {
      HookDefinitions.of(sourceType);
    }
    return factory.create(compiled);
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu) {
    if (menu.source() != null) {
      return compiler.compile(menu.source());
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource());
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu, MenuConfig config) {
    if (menu.source() != null) {
      return compiler.compile(menu.source(), config);
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource(), config);
  }

  private record PreparedReload(RegisteredMenu menu, MenuConfig config) {}

  private record PreparedReloads(List<PreparedReload> reloads, List<ReloadFailure> failures) {}
}
