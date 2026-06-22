package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.api.task.MenuTaskContext;
import com.hanielfialho.menuframework.internal.error.MenuExceptions;
import com.hanielfialho.menuframework.internal.error.MenuRuntimeLogger;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.platform.MenuScheduler;
import com.hanielfialho.menuframework.internal.render.MenuFrameApplier;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.bukkit.entity.Player;

/** Executa operações assíncronas pertencentes às sessões e valida suas gerações. */
public final class MenuAsyncTaskRuntime {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;
  private final MenuScheduler scheduler;
  private final MenuFrameApplier frames;
  private final MenuRuntimeLogger logger;

  public MenuAsyncTaskRuntime(
      MenuRuntimeState runtimeState,
      MenuSessionRegistry sessions,
      MenuScheduler scheduler,
      MenuFrameApplier frames,
      MenuRuntimeLogger logger) {
    this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.frames = Objects.requireNonNull(frames, "frames");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  private static Throwable unwrapCompletionFailure(Throwable throwable) {
    Throwable current = Objects.requireNonNull(throwable, "throwable");

    while ((current instanceof CompletionException || current instanceof ExecutionException)
        && current.getCause() != null) {
      current = current.getCause();
    }

    return current;
  }

  public <S, R> boolean start(
      MenuSession<S> session, Player viewer, MenuAsyncCommand<S, R> command) {
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(command, "command");

    if (this.isUnusable(session, viewer)) {
      return false;
    }

    MenuTaskHandle handle;

    try {
      handle = session.reserveTask(command.key());
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_TASK_START, session, command.key(), throwable);
      return false;
    }

    long generation = handle.generation();
    S startingState;

    try {
      startingState =
          Objects.requireNonNull(
              command.onStart().apply(session.state(), generation),
              "The asynchronous start transition returned null");
    } catch (Exception throwable) {
      session.cancelTask(handle);

      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_START_TRANSITION,
          session,
          command.key(),
          generation,
          throwable);
      return false;
    }

    try {
      if (!this.frames.renderAndApply(session, viewer, startingState)) {
        session.cancelTask(handle);
        return false;
      }
    } catch (Exception throwable) {
      session.cancelTask(handle);

      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_LOADING_RENDER, session, command.key(), generation, throwable);
      return false;
    }

    if (!this.runtimeState.running()) {
      handle.cancel();
      return false;
    }

    boolean activated = session.activateTask(handle);

    if (!activated) {
      handle.cancel();
      return false;
    }

    PendingAsync<S, R> pending =
        new PendingAsync<>(
            session.viewerId(),
            session.id(),
            session.menu().getClass(),
            session.revision(),
            handle,
            command);

    MenuTaskContext taskContext =
        new MenuTaskContext(session.id(), session.viewerId(), command.key(), generation);

    try {
      ScheduledTask scheduledTask =
          Objects.requireNonNull(
              this.scheduler.runAsyncNow(
                  task -> {
                    try {
                      if (!this.runtimeState.running() || !handle.active()) {
                        return;
                      }

                      this.invokeAsyncOperation(viewer, pending, taskContext);
                    } finally {
                      handle.untrackScheduledTask(task);
                    }
                  }),
              "The async scheduler rejected the menu task");

      handle.trackScheduledTask(scheduledTask);
      return true;
    } catch (Exception throwable) {
      if (this.scheduler.available()) {
        this.completeAsync(viewer, pending, null, throwable);
        return true;
      }

      this.abandonPendingTask(pending);
      return false;
    }
  }

  private <S, R> void invokeAsyncOperation(
      Player viewer, PendingAsync<S, R> pending, MenuTaskContext taskContext) {
    MenuTaskHandle handle = pending.handle();

    if (!this.runtimeState.running() || !handle.active()) {
      return;
    }

    CompletionStage<? extends R> stage;

    try {
      stage =
          Objects.requireNonNull(
              pending.command().operation().start(taskContext),
              "The asynchronous operation returned null");
    } catch (Exception throwable) {
      this.scheduleAsyncCompletion(viewer, pending, null, throwable);
      return;
    }

    CompletableFuture<? extends R> future;

    try {
      future =
          Objects.requireNonNull(
              stage.toCompletableFuture(), "CompletionStage#toCompletableFuture returned null");

      handle.trackCompletionFuture(future);
    } catch (Exception throwable) {
      this.scheduleAsyncCompletion(viewer, pending, null, throwable);
      return;
    }

    if (!this.runtimeState.running() || !handle.active()) {
      return;
    }

    try {
      future.whenComplete(
          (result, failure) -> {
            handle.untrackCompletionFuture(future);

            if (!this.runtimeState.running() || !handle.active()) {
              return;
            }

            Throwable effectiveFailure = failure;

            if (effectiveFailure == null && result == null) {
              effectiveFailure =
                  new NullPointerException(
                      "The asynchronous operation completed " + "with a null result");
            }

            this.scheduleAsyncCompletion(viewer, pending, result, effectiveFailure);
          });
    } catch (Exception throwable) {
      handle.untrackCompletionFuture(future);

      this.scheduleAsyncCompletion(viewer, pending, null, throwable);
    }
  }

  private <S, R> void scheduleAsyncCompletion(
      Player viewer, PendingAsync<S, R> pending, R result, Throwable failure) {
    MenuTaskHandle handle = pending.handle();

    if (!this.scheduler.available() || !handle.active()) {
      this.abandonPendingTask(pending);
      return;
    }

    try {
      ScheduledTask scheduledTask =
          this.scheduler.run(
              viewer,
              task -> {
                try {
                  if (!this.runtimeState.running()) {
                    this.abandonPendingTask(pending);
                    return;
                  }

                  this.completeAsync(viewer, pending, result, failure);
                } finally {
                  handle.untrackScheduledTask(task);
                }
              },
              () -> this.abandonPendingTask(pending));

      if (scheduledTask == null) {
        this.abandonPendingTask(pending);
        return;
      }

      handle.trackScheduledTask(scheduledTask);
    } catch (Exception throwable) {
      this.abandonPendingTask(pending);

      if (this.scheduler.available()) {
        this.logger.reportDetachedTaskFailure(
            MenuFailureOperation.ASYNC_COMPLETION_SCHEDULING,
            pending.menuType(),
            pending.viewerId(),
            pending.sessionId(),
            pending.revision(),
            pending.handle().key(),
            pending.handle().generation(),
            throwable);
      }
    }
  }

  private void abandonPendingTask(PendingAsync<?, ?> pending) {
    MenuSession<?> session = this.sessions.get(pending.viewerId());

    if (session != null && session.id().equals(pending.sessionId())) {
      session.cancelTask(pending.handle());
      return;
    }

    pending.handle().cancel();
  }

  private <S, R> void completeAsync(
      Player viewer, PendingAsync<S, R> pending, R result, Throwable failure) {
    if (!this.runtimeState.running()) {
      this.abandonPendingTask(pending);
      return;
    }

    MenuSession<?> rawSession = this.sessions.current(pending.viewerId(), pending.sessionId());

    if (rawSession == null) {
      pending.handle().cancel();
      return;
    }

    if (rawSession.isTaskCurrent(pending.handle())) {
      if (!MenuViewAccess.isSessionInventoryOpen(viewer, pending.sessionId())) {
        rawSession.cancelTask(pending.handle());
        return;
      }
    } else {
      pending.handle().cancel();
      return;
    }

    if (!rawSession.claimTaskCompletion(pending.handle())) {
      return;
    }

    this.completeAsyncTyped(rawSession, viewer, pending, result, failure);
  }

  @SuppressWarnings("unchecked")
  private <S, R> void completeAsyncTyped(
      MenuSession<?> rawSession,
      Player viewer,
      PendingAsync<S, R> pending,
      R result,
      Throwable failure) {
    MenuSession<S> session = (MenuSession<S>) rawSession;

    if (this.isUnusable(session, viewer)) {
      return;
    }

    if (failure != null) {
      Throwable effectiveFailure = unwrapCompletionFailure(failure);
      MenuExceptions.rethrowIfFatal(effectiveFailure);

      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_OPERATION,
          session,
          pending.command().key(),
          pending.handle().generation(),
          effectiveFailure);

      this.applyAsyncFailure(session, viewer, pending, effectiveFailure);
      return;
    }

    S completedState;

    try {
      completedState =
          Objects.requireNonNull(
              pending
                  .command()
                  .onSuccess()
                  .apply(session.state(), pending.handle().generation(), result),
              "The asynchronous success transition returned null");
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_SUCCESS_TRANSITION,
          session,
          pending.command().key(),
          pending.handle().generation(),
          throwable);

      this.applyAsyncFailure(session, viewer, pending, throwable);
      return;
    }

    try {
      this.frames.renderAndApply(session, viewer, completedState);
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_SUCCESS_RENDER,
          session,
          pending.command().key(),
          pending.handle().generation(),
          throwable);

      /*
       * A task já foi concluída e removida do registry. Sem uma
       * transição de falha, o menu permaneceria indefinidamente no
       * estado de loading. Reutilizamos o callback onFailure para levar
       * a sessão a um estado recuperável (normalmente ERROR/retry).
       */
      this.applyAsyncFailure(session, viewer, pending, throwable);
    }
  }

  private <S, R> void applyAsyncFailure(
      MenuSession<S> session, Player viewer, PendingAsync<S, R> pending, Throwable failure) {
    if (!this.runtimeState.running() || !this.sessions.isCurrent(session)) {
      return;
    }

    S failedState;

    try {
      failedState =
          Objects.requireNonNull(
              pending
                  .command()
                  .onFailure()
                  .apply(session.state(), pending.handle().generation(), failure),
              "The asynchronous failure transition returned null");
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_FAILURE_TRANSITION,
          session,
          pending.command().key(),
          pending.handle().generation(),
          throwable);
      return;
    }

    try {
      this.frames.renderAndApply(session, viewer, failedState);
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.ASYNC_FAILURE_RENDER,
          session,
          pending.command().key(),
          pending.handle().generation(),
          throwable);
    }
  }

  private boolean isUnusable(MenuSession<?> session, Player viewer) {
    return !this.runtimeState.running()
        || !this.sessions.isCurrent(session)
        || !MenuViewAccess.isSessionInventoryOpen(viewer, session.id());
  }

  private record PendingAsync<S, R>(
      UUID viewerId,
      UUID sessionId,
      Class<?> menuType,
      long revision,
      MenuTaskHandle handle,
      MenuAsyncCommand<S, R> command) {

    private PendingAsync {
      Objects.requireNonNull(viewerId, "viewerId");
      Objects.requireNonNull(sessionId, "sessionId");
      Objects.requireNonNull(menuType, "menuType");
      Objects.requireNonNull(handle, "handle");
      Objects.requireNonNull(command, "command");

      if (revision <= 0L) {
        throw new IllegalArgumentException("revision must be greater than zero: " + revision);
      }

      if (!handle.key().equals(command.key())) {
        throw new IllegalArgumentException(
            "Task handle key does not match command key: " + handle.key() + " != " + command.key());
      }
    }
  }
}
