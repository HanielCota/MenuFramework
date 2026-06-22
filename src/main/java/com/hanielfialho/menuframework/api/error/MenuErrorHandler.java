package com.hanielfialho.menuframework.api.error;

import java.util.Objects;

/**
 * Central destination for non-fatal failures observed by the menu runtime.
 *
 * <p>A handler may be invoked from a viewer's entity scheduler or from an asynchronous scheduler
 * thread. Implementations must be thread-safe, should return quickly and must not access
 * region-sensitive Bukkit APIs directly.
 */
@FunctionalInterface
public interface MenuErrorHandler {

  /**
   * Creates a handler that validates and otherwise discards each context.
   *
   * @return a side-effect-free handler
   */
  static MenuErrorHandler noop() {
    return context -> Objects.requireNonNull(context, "context");
  }

  /**
   * Processes one failure observed by the framework.
   *
   * @param context non-null immutable failure context
   */
  void handle(MenuFailureContext context);

  /**
   * Returns a handler that invokes this handler followed by {@code next}.
   *
   * <p>If this handler throws, {@code next} is not invoked. The framework itself has an isolated
   * fallback logger for failures raised by the configured handler.
   *
   * @param next non-null next handler
   * @return the ordered composition
   * @throws NullPointerException if {@code next} is {@code null}
   */
  default MenuErrorHandler andThen(MenuErrorHandler next) {
    Objects.requireNonNull(next, "next");

    return context -> {
      this.handle(context);
      next.handle(context);
    };
  }
}
