package com.hanielfialho.menuframework;

import com.hanielfialho.menuframework.api.error.MenuErrorHandler;
import com.hanielfialho.menuframework.api.feedback.MenuFeedback;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration for one {@link MenuFramework} runtime.
 *
 * <p>Instances are thread-safe and may be reused when creating independent framework runtimes.
 */
public final class MenuFrameworkConfiguration {

  /** Default number of previous menus retained for each viewer. */
  public static final int DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH = 32;

  private static final MenuFrameworkConfiguration DEFAULTS =
      new MenuFrameworkConfiguration(
          null, DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH, MenuTheme.defaults(), MenuFeedback.none());

  private final MenuErrorHandler errorHandler;
  private final int maxNavigationHistoryDepth;
  private final MenuTheme defaultTheme;
  private final MenuFeedback defaultFeedback;

  private MenuFrameworkConfiguration(
      MenuErrorHandler errorHandler,
      int maxNavigationHistoryDepth,
      MenuTheme defaultTheme,
      MenuFeedback defaultFeedback) {
    this.errorHandler = errorHandler;
    this.maxNavigationHistoryDepth = validateHistoryDepth(maxNavigationHistoryDepth);
    this.defaultTheme = Objects.requireNonNull(defaultTheme, "defaultTheme");
    this.defaultFeedback = Objects.requireNonNull(defaultFeedback, "defaultFeedback");
  }

  /**
   * Returns the shared default configuration.
   *
   * @return default immutable configuration
   */
  public static MenuFrameworkConfiguration defaults() {
    return DEFAULTS;
  }

  /**
   * Creates a builder initialized with default values.
   *
   * @return new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the configured error handler, when supplied.
   *
   * @return optional custom error handler
   */
  public Optional<MenuErrorHandler> errorHandler() {
    return Optional.ofNullable(this.errorHandler);
  }

  /**
   * Returns the maximum number of history entries retained per viewer.
   *
   * @return positive history-depth limit
   */
  public int maxNavigationHistoryDepth() {
    return this.maxNavigationHistoryDepth;
  }

  /**
   * Returns the framework-level theme supplied to each menu during opening.
   *
   * @return non-null theme
   */
  public MenuTheme defaultTheme() {
    return this.defaultTheme;
  }

  /**
   * Returns the framework-level feedback destination supplied to each menu during opening.
   *
   * @return non-null feedback destination
   */
  public MenuFeedback defaultFeedback() {
    return this.defaultFeedback;
  }

  /**
   * Creates an independent builder initialized from this configuration.
   *
   * @return populated builder
   */
  public Builder toBuilder() {
    Builder builder =
        new Builder()
            .maxNavigationHistoryDepth(this.maxNavigationHistoryDepth)
            .defaultTheme(this.defaultTheme)
            .defaultFeedback(this.defaultFeedback);

    if (this.errorHandler != null) {
      builder.errorHandler(this.errorHandler);
    }

    return builder;
  }

  private static int validateHistoryDepth(int value) {
    if (value <= 0) {
      throw new IllegalArgumentException(
          "maxNavigationHistoryDepth must be greater than zero: " + value);
    }
    return value;
  }

  /** Mutable, non-thread-safe builder for {@link MenuFrameworkConfiguration}. */
  public static final class Builder {

    private MenuErrorHandler errorHandler;
    private int maxNavigationHistoryDepth = DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH;
    private MenuTheme defaultTheme = MenuTheme.defaults();
    private MenuFeedback defaultFeedback = MenuFeedback.none();

    private Builder() {}

    /**
     * Sets the central destination for non-fatal runtime failures.
     *
     * @param errorHandler thread-safe error handler
     * @return this builder
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
     * @param maxDepth positive maximum depth
     * @return this builder
     */
    public Builder maxNavigationHistoryDepth(int maxDepth) {
      this.maxNavigationHistoryDepth = validateHistoryDepth(maxDepth);
      return this;
    }

    /**
     * Sets the framework-level default theme.
     *
     * @param defaultTheme non-null theme
     * @return this builder
     */
    public Builder defaultTheme(MenuTheme defaultTheme) {
      this.defaultTheme = Objects.requireNonNull(defaultTheme, "defaultTheme");
      return this;
    }

    /**
     * Sets the framework-level feedback destination.
     *
     * <p>Use {@link MenuFeedback#none()} to disable feedback globally.
     *
     * @param defaultFeedback non-null feedback destination
     * @return this builder
     */
    public Builder defaultFeedback(MenuFeedback defaultFeedback) {
      this.defaultFeedback = Objects.requireNonNull(defaultFeedback, "defaultFeedback");
      return this;
    }

    /**
     * Builds an immutable configuration snapshot.
     *
     * @return new configuration
     */
    public MenuFrameworkConfiguration build() {
      return new MenuFrameworkConfiguration(
          this.errorHandler,
          this.maxNavigationHistoryDepth,
          this.defaultTheme,
          this.defaultFeedback);
    }
  }
}
