package com.hanielfialho.menuframework.internal.error;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.error.DefaultMenuErrorHandler;
import com.hanielfialho.menuframework.api.error.MenuErrorHandler;
import com.hanielfialho.menuframework.api.error.MenuFailureContext;
import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Despacha falhas para o handler configurado e mantém um fallback isolado.
 *
 * <p>O nome foi preservado para evitar churn interno, mas esta classe não decide mais como a
 * aplicação registra ou exporta uma falha.
 */
public final class MenuRuntimeLogger {

  private final MenuErrorHandler errorHandler;
  private final Logger fallbackLogger;

  public MenuRuntimeLogger(Plugin plugin, MenuErrorHandler errorHandler) {
    Objects.requireNonNull(plugin, "plugin");

    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    this.fallbackLogger = plugin.getLogger();
  }

  public void reportOpenFailure(
      MenuFailureOperation operation, Player viewer, Menu<?> menu, Throwable throwable) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(menu, "menu");

    MenuFailureContext context =
        MenuFailureContext.builder(operation, throwable, viewer.getUniqueId(), menu.getClass())
            .build();

    this.dispatch(context);
  }

  public void reportSessionFailure(
      MenuFailureOperation operation, MenuSession<?> session, Throwable throwable) {
    Objects.requireNonNull(session, "session");

    MenuFailureContext context = this.sessionBuilder(operation, session, throwable).build();

    this.dispatch(context);
  }

  public void reportTaskFailure(
      MenuFailureOperation operation,
      MenuSession<?> session,
      MenuTaskKey taskKey,
      Throwable throwable) {
    Objects.requireNonNull(taskKey, "taskKey");

    MenuFailureContext context =
        this.sessionBuilder(operation, session, throwable).task(taskKey).build();

    this.dispatch(context);
  }

  public void reportTaskFailure(
      MenuFailureOperation operation,
      MenuSession<?> session,
      MenuTaskKey taskKey,
      long generation,
      Throwable throwable) {
    Objects.requireNonNull(taskKey, "taskKey");

    MenuFailureContext context =
        this.sessionBuilder(operation, session, throwable).task(taskKey, generation).build();

    this.dispatch(context);
  }

  public void reportTaskFailure(
      MenuFailureOperation operation,
      MenuSession<?> session,
      MenuTaskKey taskKey,
      long generation,
      long execution,
      Throwable throwable) {
    Objects.requireNonNull(taskKey, "taskKey");

    MenuFailureContext context =
        this.sessionBuilder(operation, session, throwable)
            .task(taskKey, generation, execution)
            .build();

    this.dispatch(context);
  }

  public void reportDetachedTaskFailure(
      MenuFailureOperation operation,
      Class<?> menuType,
      UUID viewerId,
      UUID sessionId,
      long revision,
      MenuTaskKey taskKey,
      long generation,
      Throwable throwable) {
    MenuFailureContext context =
        MenuFailureContext.builder(operation, throwable, viewerId, menuType)
            .session(sessionId, revision)
            .task(taskKey, generation)
            .build();

    this.dispatch(context);
  }

  private MenuFailureContext.Builder sessionBuilder(
      MenuFailureOperation operation, MenuSession<?> session, Throwable throwable) {
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(throwable, "throwable");

    return MenuFailureContext.builder(
            operation, throwable, session.viewerId(), session.menu().getClass())
        .session(session.id(), session.revision());
  }

  private void dispatch(MenuFailureContext context) {
    Objects.requireNonNull(context, "context");

    try {
      this.errorHandler.handle(context);
    } catch (Exception handlerFailure) {
      this.logFallback(
          "MenuErrorHandler failed while processing " + context.operation(), handlerFailure);

      this.logFallback(DefaultMenuErrorHandler.format(context), context.cause());
    }
  }

  private void logFallback(String message, Throwable throwable) {
    try {
      this.fallbackLogger.log(Level.SEVERE, message, throwable);
    } catch (Exception reportingFailure) {
      // Non-fatal reporting failures must not escape into the runtime.
    }
  }
}
