package com.github.hanielcota.menuframework;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.builder.MenuBuilder;
import com.github.hanielcota.menuframework.internal.MenuFrameworkInitializer;
import com.github.hanielcota.menuframework.scheduler.PaperSchedulerAdapter;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

/**
 * Entry point for the MenuFramework library. Provides factory methods for creating and initializing
 * menu services.
 *
 * <p>The framework supports two modes of operation:
 *
 * <ul>
 *   <li><b>Standalone mode:</b> Use {@link #create(Plugin)} or {@link #create(Plugin, Builder)}
 *       when you manage the service lifecycle yourself.
 *   <li><b>Singleton mode:</b> Use {@link #initialize(Plugin)} or {@link #initializeOrGet(Plugin)}
 *       when you want a global shared instance accessible via {@link #service()}.
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Singleton mode (most common)
 * MenuService service = MenuFramework.initialize(plugin);
 *
 * // Or with custom configuration
 * MenuService service = MenuFramework.initialize(plugin, MenuFramework.builder()
 *     .config(new MenuFrameworkConfig().sessionCacheMaxSize(1000))
 *     .scheduler(new PaperSchedulerAdapter())
 *     .build());
 * }</pre>
 *
 * @see MenuService
 * @see MenuBuilder
 */
public final class MenuFramework {

  private static final AtomicReference<MenuService> SERVICE = new AtomicReference<>();
  private static final ReentrantLock LOCK = new ReentrantLock();

  private MenuFramework() {}

  /**
   * Creates a new menu builder for the given menu ID using the current singleton service.
   *
   * @param id the unique menu identifier
   * @return a new menu builder
   * @throws IllegalStateException if the framework has not been initialized in singleton mode
   */
  public static @NonNull MenuBuilder builder(@NonNull String id) {
    return new MenuBuilder(id, service());
  }

  /**
   * Creates a new menu builder for the given menu ID and service.
   *
   * @param id the unique menu identifier
   * @param service the menu service to register the menu with
   * @return a new menu builder
   */
  public static @NonNull MenuBuilder builder(@NonNull String id, @NonNull MenuService service) {
    return new MenuBuilder(id, service);
  }

  /**
   * Returns the singleton menu service instance.
   *
   * @return the current singleton service
   * @throws IllegalStateException if no singleton has been initialized
   */
  public static @NonNull MenuService service() {
    var service = SERVICE.get();
    if (service == null) {
      throw new IllegalStateException(
          "MenuFramework not initialized. Call initialize() or initializeOrGet() first.");
    }
    return service;
  }

  /**
   * Creates a new standalone menu service with default configuration.
   *
   * <p>The returned service is not stored as a singleton and must be managed by the caller.
   *
   * @param plugin the owning plugin
   * @return a new menu service instance
   */
  public static @NonNull MenuService create(@NonNull Plugin plugin) {
    return createService(plugin, new Builder());
  }

  /**
   * Creates a new standalone menu service with custom configuration.
   *
   * <p>The returned service is not stored as a singleton and must be managed by the caller.
   *
   * @param plugin the owning plugin
   * @param builder the configuration builder
   * @return a new menu service instance
   */
  public static @NonNull MenuService create(@NonNull Plugin plugin, @NonNull Builder builder) {
    return createService(plugin, builder);
  }

  /**
   * Initializes the framework in singleton mode with default configuration.
   *
   * <p>The service is stored globally and accessible via {@link #service()}.
   *
   * @param plugin the owning plugin
   * @return the initialized menu service
   * @throws IllegalStateException if a singleton already exists
   */
  public static @NonNull MenuService initialize(@NonNull Plugin plugin) {
    return initialize(plugin, new Builder());
  }

  /**
   * Initializes the framework in singleton mode with custom configuration.
   *
   * <p>The service is stored globally and accessible via {@link #service()}.
   *
   * @param plugin the owning plugin
   * @param builder the configuration builder
   * @return the initialized menu service
   * @throws IllegalStateException if a singleton already exists
   */
  public static @NonNull MenuService initialize(@NonNull Plugin plugin, @NonNull Builder builder) {
    var service = createService(plugin, builder);
    if (!SERVICE.compareAndSet(null, service)) {
      throw new IllegalStateException("MenuFramework already initialized");
    }
    return service;
  }

  /**
   * Initializes the framework in singleton mode if not already initialized, otherwise returns the
   * existing instance.
   *
   * <p>This method is thread-safe and performs a double-checked locking pattern.
   *
   * @param plugin the owning plugin
   * @return the existing or newly initialized menu service
   */
  public static @NonNull MenuService initializeOrGet(@NonNull Plugin plugin) {
    return initializeOrGet(plugin, new Builder());
  }

  /**
   * Initializes the framework in singleton mode with custom configuration if not already
   * initialized, otherwise returns the existing instance.
   *
   * <p>This method is thread-safe and performs a double-checked locking pattern.
   *
   * @param plugin the owning plugin
   * @param builder the configuration builder
   * @return the existing or newly initialized menu service
   */
  public static @NonNull MenuService initializeOrGet(
      @NonNull Plugin plugin, @NonNull Builder builder) {
    var existing = SERVICE.get();
    if (existing != null) return existing;
    var service = createService(plugin, builder);
    if (SERVICE.compareAndSet(null, service)) {
      return service;
    }
    return service();
  }

  /**
   * Forcefully reinitializes the singleton service.
   *
   * <p>Shuts down the existing service (if any) and creates a new one.
   *
   * @param plugin the owning plugin
   * @return the newly initialized menu service
   */
  public static @NonNull MenuService forceReinitialize(@NonNull Plugin plugin) {
    return forceReinitialize(plugin, new Builder());
  }

  /**
   * Forcefully reinitializes the singleton service with custom configuration.
   *
   * <p>Shuts down the existing service (if any) and creates a new one.
   *
   * @param plugin the owning plugin
   * @param builder the configuration builder
   * @return the newly initialized menu service
   */
  public static @NonNull MenuService forceReinitialize(
      @NonNull Plugin plugin, @NonNull Builder builder) {
    LOCK.lock();
    try {
      shutdown();
      return initialize(plugin, builder);
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * Shuts down the singleton service if it exists.
   *
   * <p>Idempotent - safe to call multiple times.
   */
  public static void shutdown() {
    var service = SERVICE.getAndSet(null);
    if (service != null) {
      service.shutdown();
    }
  }

  private static @NonNull MenuService createService(
      @NonNull Plugin plugin, @NonNull Builder builder) {
    Objects.requireNonNull(plugin, "plugin");
    var scheduler = builder.scheduler != null ? builder.scheduler : new PaperSchedulerAdapter();
    var config = builder.config != null ? builder.config : new MenuFrameworkConfig();
    return MenuFrameworkInitializer.initialize(plugin, scheduler, config);
  }

  /**
   * Builder for configuring MenuFramework initialization.
   *
   * <p>Example:
   *
   * <pre>{@code
   * MenuService service = MenuFramework.create(plugin, MenuFramework.builder()
   *     .config(new MenuFrameworkConfig().sessionCacheMaxSize(1000))
   *     .scheduler(new PaperSchedulerAdapter())
   *     .build());
   * }</pre>
   */
  public static final class Builder {
    private SchedulerAdapter scheduler;
    private MenuFrameworkConfig config;

    private Builder() {}

    /**
     * Sets the scheduler adapter. Defaults to {@link PaperSchedulerAdapter}.
     *
     * @param scheduler the scheduler implementation
     * @return this builder
     */
    public @NonNull Builder scheduler(@NonNull SchedulerAdapter scheduler) {
      this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
      return this;
    }

    /**
     * Sets the framework configuration. Defaults to {@link MenuFrameworkConfig} with default
     * values.
     *
     * @param config the configuration
     * @return this builder
     */
    public @NonNull Builder config(@NonNull MenuFrameworkConfig config) {
      this.config = Objects.requireNonNull(config, "config");
      return this;
    }
  }
}
