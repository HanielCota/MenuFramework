package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.api.task.MenuTickContext;
import com.hanielfialho.menuframework.api.task.MenuTickResult;
import com.hanielfialho.menuframework.internal.error.MenuRuntimeLogger;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.platform.MenuScheduler;
import com.hanielfialho.menuframework.internal.render.MenuFrameApplier;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.entity.Player;

/** Agenda, executa e cancela tarefas periódicas pertencentes às sessões. */
public final class MenuPeriodicTaskRuntime {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;
  private final MenuScheduler scheduler;
  private final MenuFrameApplier frames;
  private final MenuRuntimeLogger logger;

  public MenuPeriodicTaskRuntime(
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

  public <S> void startAll(
      MenuSession<S> session, Player viewer, List<MenuPeriodicCommand<S>> commands) {
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(commands, "commands");

    for (MenuPeriodicCommand<S> command : commands) {
      if (this.isUnusable(session, viewer)) {
        return;
      }

      this.start(session, viewer, command);
    }
  }

  private <S> void start(MenuSession<S> session, Player viewer, MenuPeriodicCommand<S> command) {
    if (this.isUnusable(session, viewer)) {
      return;
    }

    MenuTaskHandle handle;

    try {
      handle = session.reserveTask(command.key());
    } catch (Exception throwable) {
      this.logger.reportTaskFailure(
          MenuFailureOperation.PERIODIC_TASK_START, session, command.key(), throwable);
      return;
    }

    PendingPeriodic<S> pending =
        new PendingPeriodic<>(
            session.viewerId(),
            session.id(),
            handle,
            command,
            new AtomicLong(),
            new AtomicBoolean());

    try {
      ScheduledTask scheduledTask =
          this.scheduler.runAtFixedRate(
              viewer,
              ignored -> this.runTick(viewer, pending),
              () -> this.abandon(pending),
              command.schedule().initialDelayTicks(),
              command.schedule().periodTicks());

      if (scheduledTask == null) {
        this.abandon(pending);
        return;
      }

      handle.trackScheduledTask(scheduledTask);

      if (session.activateTask(handle)) {
        return;
      }

      handle.cancel();
    } catch (Exception throwable) {
      this.abandon(pending);

      if (this.scheduler.available()) {
        this.logger.reportTaskFailure(
            MenuFailureOperation.PERIODIC_TASK_SCHEDULING,
            session,
            command.key(),
            handle.generation(),
            throwable);
      }
    }
  }

  private <S> void runTick(Player viewer, PendingPeriodic<S> pending) {
    if (!pending.executing().compareAndSet(false, true)) {
      return;
    }

    try {
      if (!this.runtimeState.running()) {
        this.abandon(pending);
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

        this.runTickTyped(rawSession, viewer, pending);
        return;
      }

      pending.handle().cancel();
    } finally {
      pending.executing().set(false);
    }
  }

  @SuppressWarnings("unchecked")
  private <S> void runTickTyped(
      MenuSession<?> rawSession, Player viewer, PendingPeriodic<S> pending) {
    MenuSession<S> session = (MenuSession<S>) rawSession;

    long execution;

    try {
      execution = pending.executions().updateAndGet(Math::incrementExact);
    } catch (Exception throwable) {
      session.cancelTask(pending.handle());

      this.logger.reportTaskFailure(
          MenuFailureOperation.PERIODIC_EXECUTION_COUNTER,
          session,
          pending.command().key(),
          pending.handle().generation(),
          throwable);
      return;
    }

    MenuTickContext<S> context =
        new MenuTickContext<>(
            session.id(),
            viewer,
            session.state(),
            session.revision(),
            pending.command().key(),
            pending.handle().generation(),
            execution);

    MenuTickResult<S> result;

    try {
      result =
          Objects.requireNonNull(
              pending.command().task().tick(context), "The periodic menu task returned null");
    } catch (Exception throwable) {
      session.cancelTask(pending.handle());

      this.logger.reportTaskFailure(
          MenuFailureOperation.PERIODIC_EXECUTION,
          session,
          pending.command().key(),
          pending.handle().generation(),
          execution,
          throwable);
      return;
    }

    if (!(this.runtimeState.running()
        && this.sessions.isCurrent(session)
        && session.isTaskCurrent(pending.handle())
        && MenuViewAccess.isSessionInventoryOpen(viewer, session.id()))) {
      session.cancelTask(pending.handle());
      return;
    }

    if (result.renderRequested()) {
      S candidateState = result.resolveState(session.state());

      try {
        if (!this.frames.renderAndApply(session, viewer, candidateState)) {
          session.cancelTask(pending.handle());
          return;
        }
      } catch (Exception throwable) {
        session.cancelTask(pending.handle());

        this.logger.reportTaskFailure(
            MenuFailureOperation.PERIODIC_RENDER,
            session,
            pending.command().key(),
            pending.handle().generation(),
            execution,
            throwable);
        return;
      }
    }

    if (result.stopRequested()) {
      session.cancelTask(pending.handle());
    }
  }

  private void abandon(PendingPeriodic<?> pending) {
    MenuSession<?> session = this.sessions.get(pending.viewerId());

    if (session != null && session.id().equals(pending.sessionId())) {
      session.cancelTask(pending.handle());
      return;
    }

    pending.handle().cancel();
  }

  private boolean isUnusable(MenuSession<?> session, Player viewer) {
    return !this.runtimeState.running()
        || !this.sessions.isCurrent(session)
        || !MenuViewAccess.isSessionInventoryOpen(viewer, session.id());
  }

  private record PendingPeriodic<S>(
      UUID viewerId,
      UUID sessionId,
      MenuTaskHandle handle,
      MenuPeriodicCommand<S> command,
      AtomicLong executions,
      AtomicBoolean executing) {

    private PendingPeriodic {
      Objects.requireNonNull(viewerId, "viewerId");
      Objects.requireNonNull(sessionId, "sessionId");
      Objects.requireNonNull(handle, "handle");
      Objects.requireNonNull(command, "command");
      Objects.requireNonNull(executions, "executions");
      Objects.requireNonNull(executing, "executing");

      if (!handle.key().equals(command.key())) {
        throw new IllegalArgumentException(
            "Task handle key does not match periodic "
                + "command key: "
                + handle.key()
                + " != "
                + command.key());
      }
    }
  }
}
