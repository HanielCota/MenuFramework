package dev.haniel.menu.paper;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuSession;
import dev.haniel.menu.paper.discovery.MenuScanner;
import dev.haniel.menu.paper.holder.OpenMenu;
import dev.haniel.menu.paper.registry.MenuRegistry;
import dev.haniel.menu.paper.registry.ReloadReport;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The public Paper/Folia facade for MenuFramework.
 *
 * <p>Application code should depend on this class. Registry, discovery, compiler, scheduler and
 * listener remain available for advanced integrations, but normal plugins only need the facade.
 */
public final class MenuFramework {

  private final MenuRegistry registry;
  private final MenuScanner scanner;
  private final MenuLifecycle lifecycle;

  MenuFramework(MenuRegistry registry, MenuScanner scanner, MenuLifecycle lifecycle) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.scanner = Objects.requireNonNull(scanner, "scanner");
    this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
  }

  /**
   * Starts a builder for a plugin-owned framework instance.
   *
   * @param plugin the owning plugin; never null
   * @return a new builder
   */
  public static MenuFrameworkBuilder builder(JavaPlugin plugin) {
    return new MenuFrameworkBuilder(plugin);
  }

  /**
   * Discovers and registers every {@code @Menu} under the given packages.
   *
   * @param basePackages packages to scan; never empty
   * @return this framework
   */
  public MenuFramework scan(String... basePackages) {
    PackageNames.requireValid(basePackages);
    scanner.scanTypes(Set.of(basePackages), registry::register);
    return this;
  }

  /**
   * Manually registers one annotated menu instance.
   *
   * @param menu an object whose class is annotated with {@code @Menu}; never null
   * @return this framework
   */
  public MenuFramework register(Object menu) {
    registry.register(menu);
    return this;
  }

  /**
   * Opens a registered menu by id.
   *
   * @param player the viewer; never null
   * @param id the menu id; never null
   */
  public void open(Player player, MenuId id) {
    registry.open(player, id);
  }

  /**
   * Opens a registered menu by id, passing it a typed open argument.
   *
   * <p>The argument is injected into any {@code @Arg} field of a paginated menu before its first
   * render, so the menu can be opened "for" a target, an amount or any context without a
   * hand-rolled session carrier. See {@link dev.haniel.menu.annotation.Arg}.
   *
   * @param player the viewer; never null
   * @param id the menu id; never null
   * @param argument the open argument, or {@code null} for none
   */
  public void open(Player player, MenuId id, Object argument) {
    registry.open(player, id, argument);
  }

  /**
   * Opens a registered menu by its annotated class.
   *
   * @param player the viewer; never null
   * @param menuType the registered menu class; never null
   */
  public void open(Player player, Class<?> menuType) {
    registry.open(player, menuType);
  }

  /**
   * Opens a registered menu by its annotated class, passing it a typed open argument.
   *
   * @param player the viewer; never null
   * @param menuType the registered menu class; never null
   * @param argument the open argument, or {@code null} for none
   */
  public void open(Player player, Class<?> menuType, Object argument) {
    registry.open(player, menuType, argument);
  }

  /**
   * Returns a handle to the menu the given player currently has open, if it is one of this
   * framework's menus.
   *
   * <p>The handle is a transient snapshot — query it again rather than caching it. Use it to
   * refresh or close a player's menu in reaction to a domain event, without holding the menu
   * instance.
   *
   * @param player the player to inspect; never null
   * @return the open menu session, or {@link Optional#empty()} if no framework menu is open
   */
  public Optional<MenuSession> session(Player player) {
    InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
    if (holder instanceof OpenMenu menu) {
      return Optional.of(new MenuSession(player, menu));
    }
    return Optional.empty();
  }

  /**
   * Returns a handle to the menu the identified player currently has open, if they are online and
   * viewing one of this framework's menus.
   *
   * @param id the player's id; never null
   * @return the open menu session, or {@link Optional#empty()} if the player is offline or has no
   *     framework menu open
   */
  public Optional<MenuSession> session(PlayerId id) {
    Player player = Bukkit.getPlayer(id.value());
    return player == null ? Optional.empty() : session(player);
  }

  /**
   * Reloads one registered menu from its YAML.
   *
   * @param id the menu id; never null
   * @return {@code true} if the menu existed and was reloaded
   */
  public boolean reload(MenuId id) {
    return registry.reload(id);
  }

  /**
   * Reloads one registered menu from YAML and reports the result.
   *
   * @param id the menu id; never null
   * @return the reload report
   */
  public ReloadReport reloadReport(MenuId id) {
    return registry.reloadReport(id);
  }

  /**
   * Reloads every registered menu from YAML.
   *
   * @return the number of menus reloaded
   */
  public int reloadAll() {
    return registry.reloadAll();
  }

  /**
   * Reloads every registered menu from YAML and reports successes and failures.
   *
   * @return the reload report
   */
  public ReloadReport reloadAllReport() {
    return registry.reloadAllReport();
  }

  /**
   * Reloads every registered menu with YAML IO off the main thread.
   *
   * <p>Only file IO and YAML parsing run asynchronously. The compiled menu swap runs on the plugin
   * scheduler because rendering creates Bukkit {@code ItemStack}s.
   *
   * <p>Never block the server thread on the returned future ({@code join()}/{@code get()}): the
   * apply stage needs that same thread, so blocking it deadlocks. Attach a callback instead.
   *
   * @return a future completed with the reload report
   */
  public CompletableFuture<ReloadReport> reloadAllReportAsync() {
    return registry.reloadAllReportAsync(lifecycle.asyncExecutor(), lifecycle.syncExecutor());
  }

  /**
   * Closes one player's current inventory.
   *
   * @param player the player to close; never null
   */
  public void close(Player player) {
    player.closeInventory();
  }

  /** Unregisters the listener, cancels tasks, closes open menus and clears the registry. */
  public void shutdown() {
    lifecycle.shutdown();
    registry.clear();
  }
}
