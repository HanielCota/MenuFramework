package com.hanielfialho.menuframework;

import com.hanielfialho.menuframework.internal.inventory.MenuListener;
import java.util.Objects;
import org.bukkit.plugin.Plugin;

/**
 * Entry point used to install and access the menu runtime.
 *
 * <p>Create exactly one instance for each plugin during {@code JavaPlugin#onEnable()}, keep it in a
 * field and invoke {@link #shutdown()} from {@code JavaPlugin#onDisable()}.
 */
public final class MenuFramework {

  private final MenuFrameworkConfiguration configuration;
  private final MenuManager menuManager;

  private MenuFramework(Plugin plugin, MenuFrameworkConfiguration configuration) {
    Plugin checkedPlugin = Objects.requireNonNull(plugin, "plugin");

    if (!checkedPlugin.isEnabled()) {
      throw new IllegalStateException(
          "MenuFramework must be created while the owning plugin "
              + "is enabled (normally from JavaPlugin#onEnable)");
    }

    this.configuration = Objects.requireNonNull(configuration, "configuration");
    this.menuManager = new MenuManager(checkedPlugin, configuration);

    try {
      checkedPlugin
          .getServer()
          .getPluginManager()
          .registerEvents(new MenuListener(this.menuManager.eventHandler()), checkedPlugin);
    } catch (RuntimeException | Error failure) {
      this.menuManager.shutdown();
      throw failure;
    }
  }

  /**
   * Creates and registers a framework using the default configuration.
   *
   * @param plugin owning plugin
   * @return registered framework
   * @throws NullPointerException if {@code plugin} is {@code null}
   * @throws IllegalStateException if the plugin is not enabled
   */
  public static MenuFramework create(Plugin plugin) {
    return create(plugin, MenuFrameworkConfiguration.defaults());
  }

  /**
   * Creates and registers a framework.
   *
   * @param plugin owning plugin
   * @param configuration immutable runtime configuration
   * @return registered framework
   * @throws NullPointerException if an argument is {@code null}
   * @throws IllegalStateException if the plugin is not enabled
   */
  public static MenuFramework create(Plugin plugin, MenuFrameworkConfiguration configuration) {
    return new MenuFramework(plugin, configuration);
  }

  /**
   * Returns the immutable configuration used by this instance.
   *
   * @return configuration
   */
  public MenuFrameworkConfiguration configuration() {
    return this.configuration;
  }

  /**
   * Returns the public menu manager.
   *
   * @return manager
   */
  public MenuManager menus() {
    return this.menuManager;
  }

  /**
   * Returns whether this framework has been shut down.
   *
   * @return shutdown flag
   */
  public boolean isShutdown() {
    return this.menuManager.isShutdown();
  }

  /**
   * Invalidates every session, clears navigation history and cancels every session-owned task.
   *
   * <p>This operation is idempotent and intended exclusively for {@code JavaPlugin#onDisable()}. It
   * intentionally does not invoke menu callbacks or manipulate inventory views, because plugin
   * shutdown has no guaranteed entity-region context on Folia. Paper unregisters the plugin's
   * listeners as part of the normal disable lifecycle.
   */
  public void shutdown() {
    this.menuManager.shutdown();
  }
}
