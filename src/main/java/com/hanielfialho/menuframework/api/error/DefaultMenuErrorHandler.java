package com.hanielfialho.menuframework.api.error;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

/**
 * Default error handler that writes structured metadata and the original stack trace to a {@link
 * Logger}.
 */
public final class DefaultMenuErrorHandler implements MenuErrorHandler {

  private final Logger logger;

  /**
   * Creates a handler backed by an owning plugin's logger.
   *
   * @param plugin non-null owning plugin
   * @throws NullPointerException if {@code plugin} is {@code null}
   */
  public DefaultMenuErrorHandler(Plugin plugin) {
    this(Objects.requireNonNull(plugin, "plugin").getLogger());
  }

  /**
   * Creates a handler backed by an explicit logger.
   *
   * @param logger non-null destination logger
   * @throws NullPointerException if {@code logger} is {@code null}
   */
  public DefaultMenuErrorHandler(Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  /**
   * Formats the structural metadata without rendering the stack trace.
   *
   * @param context non-null failure context
   * @return a stable structured message
   * @throws NullPointerException if {@code context} is {@code null}
   */
  public static String format(MenuFailureContext context) {
    Objects.requireNonNull(context, "context");

    StringJoiner attributes = new StringJoiner(", ", "Menu failure [", "]");

    attributes.add("operation=" + context.operation());
    attributes.add("menu=" + context.menuTypeName());
    attributes.add("player=" + context.viewerId());

    context.sessionId().ifPresent(value -> attributes.add("session=" + value));
    context.revision().ifPresent(value -> attributes.add("revision=" + value));
    context.taskKey().ifPresent(value -> attributes.add("task=" + value.value()));
    context.taskGeneration().ifPresent(value -> attributes.add("generation=" + value));
    context.taskExecution().ifPresent(value -> attributes.add("execution=" + value));

    return attributes + ": " + context.operation().description();
  }

  /** {@inheritDoc} */
  @Override
  public void handle(MenuFailureContext context) {
    Objects.requireNonNull(context, "context");

    this.logger.log(Level.SEVERE, format(context), context.cause());
  }
}
