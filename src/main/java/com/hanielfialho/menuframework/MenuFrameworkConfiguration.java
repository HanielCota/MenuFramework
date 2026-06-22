package com.hanielfialho.menuframework;

import com.hanielfialho.menuframework.api.error.MenuErrorHandler;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Immutable configuration for one {@link MenuFramework} runtime.
 *
 * <p>Use {@link #builder()} to install a custom error handler or to change the bounded
 * navigation-history depth. Instances are thread-safe and may be reused when creating independent
 * framework runtimes.
 */
public final class MenuFrameworkConfiguration {

  /** Default number of previous menus retained for each viewer. */
  public static final int DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH = 32;

  private static final MenuFrameworkConfiguration DEFAULTS =
      new MenuFrameworkConfiguration(null, DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH);

  private final @Nullable MenuErrorHandler errorHandler;
  private final int maxNavigationHistoryDepth;

  private MenuFrameworkConfiguration(
      @Nullable MenuErrorHandler errorHandler, int maxNavigationHistoryDepth) {
    this.errorHandler = errorHandler;
    this.maxNavigationHistoryDepth = validateHistoryDepth(maxNavigationHistoryDepth);
  }

  /**
   * Returns the shared default configuration.
   *
   * @return the default immutable configuration
   */
  public static MenuFrameworkConfiguration defaults() {
    return DEFAULTS;
  }

  /**
   * Creates a builder initialized with the default values.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private static int validateHistoryDepth(int value) {
    if (value <= 0) {
      throw new IllegalArgumentException(
          "maxNavigationHistoryDepth must be greater " + "than zero: " + value);
    }

    return value;
  }

  /**
   * Returns the configured error handler, when one was supplied.
   *
   * <p>When empty, the framework creates a {@link
   * com.hanielfialho.menuframework.api.error.DefaultMenuErrorHandler} backed by the owning plugin's
   * logger.
   *
   * @return the optional custom error handler
   */
  public Optional<MenuErrorHandler> errorHandler() {
    return Optional.ofNullable(this.errorHandler);
  }

  /**
   * Returns the maximum number of history entries retained per viewer.
   *
   * @return a positive history-depth limit
   */
  public int maxNavigationHistoryDepth() {
    return this.maxNavigationHistoryDepth;
  }

  /**
   * Creates an independent builder initialized from this configuration.
   *
   * @return a populated builder
   */
  public Builder toBuilder() {
    return new Builder()
        .errorHandlerIfPresent(this.errorHandler)
        .maxNavigationHistoryDepth(this.maxNavigationHistoryDepth);
  }

  /**
   * Mutable builder for {@link MenuFrameworkConfiguration}.
   *
   * <p>The builder is not thread-safe. The configuration returned by {@link #build()} is immutable.
   */
  public static final class Builder {

    private @Nullable MenuErrorHandler errorHandler;
    private int maxNavigationHistoryDepth = DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH;

    private Builder() {}

    /**
     * Sets the central destination for non-fatal runtime failures.
     *
     * <p>The handler can be invoked from entity-scheduler and asynchronous scheduler threads. It
     * must therefore be thread-safe and must not call region-sensitive Bukkit APIs unless it
     * explicitly reschedules that work.
     *
     * @param errorHandler non-null handler
     * @return this builder
     * @throws NullPointerException if {@code errorHandler} is {@code null}
     */
    public Builder errorHandler(MenuErrorHandler errorHandler) {
      this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
      return this;
    }

    /**
     * Removes a custom handler so the plugin-backed default logger is used.
     *
     * @return this builder
     */
    public Builder useDefaultErrorHandler() {
      this.errorHandler = null;
      return this;
    }

    /**
     * Sets the maximum navigation-history depth per viewer.
     *
     * <p>When the limit is reached, the oldest entry is discarded before a new entry is appended.
     *
     * @param maxDepth positive maximum depth
     * @return this builder
     * @throws IllegalArgumentException if {@code maxDepth} is not positive
     */
    public Builder maxNavigationHistoryDepth(int maxDepth) {
      this.maxNavigationHistoryDepth = validateHistoryDepth(maxDepth);
      return this;
    }

    /**
     * Builds an immutable configuration snapshot.
     *
     * @return a new configuration
     */
    public MenuFrameworkConfiguration build() {
      return new MenuFrameworkConfiguration(this.errorHandler, this.maxNavigationHistoryDepth);
    }

    private Builder errorHandlerIfPresent(@Nullable MenuErrorHandler errorHandler) {
      if (errorHandler != null) {
        this.errorHandler = errorHandler;
      }

      return this;
    }
  }
}
