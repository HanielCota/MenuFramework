package com.hanielfialho.menuframework.internal.interaction;

import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.task.MenuPeriodicTask;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import com.hanielfialho.menuframework.internal.task.MenuAsyncCommand;
import com.hanielfialho.menuframework.internal.task.MenuTaskCommands;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Buffer interno de comandos produzidos pelo callback de abertura.
 *
 * @param <S> tipo de estado da sessão
 */
public final class MenuOpenContextImpl<S> implements MenuOpenContext<S> {

  private final UUID sessionId;
  private final Player viewer;
  private final S state;
  private final long revision;
  private final int historyDepth;
  private final MenuTaskCommands<S> taskCommands;

  private MenuAsyncCommand<S, ?> asyncCommand;
  private boolean finished;

  public MenuOpenContextImpl(
      UUID sessionId, Player viewer, S state, long revision, int historyDepth) {
    this.sessionId = Objects.requireNonNull(sessionId, "sessionId");

    this.viewer = Objects.requireNonNull(viewer, "viewer");

    this.state = Objects.requireNonNull(state, "state");

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }

    this.revision = revision;

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }

    this.historyDepth = historyDepth;
    this.taskCommands = new MenuTaskCommands<>();
  }

  public MenuOpenContextImpl(UUID sessionId, Player viewer, S state, long revision) {
    this(sessionId, viewer, state, revision, 0);
  }

  @Override
  public UUID sessionId() {
    this.ensureActive();
    return this.sessionId;
  }

  @Override
  public Player viewer() {
    this.ensureActive();
    return this.viewer;
  }

  @Override
  public S state() {
    this.ensureActive();
    return this.state;
  }

  @Override
  public long revision() {
    this.ensureActive();
    return this.revision;
  }

  @Override
  public int historyDepth() {
    this.ensureActive();
    return this.historyDepth;
  }

  @Override
  public <R> void executeAsync(
      MenuTaskKey key,
      Operation<R> operation,
      Start<S> onStart,
      Success<S, R> onSuccess,
      Failure<S> onFailure) {
    this.ensureActive();
    Objects.requireNonNull(key, "key");

    if (this.asyncCommand != null) {
      throw new IllegalStateException(
          "Only one asynchronous command can be " + "requested from Menu#onOpen");
    }

    if (this.taskCommands.contains(key)) {
      throw new IllegalStateException(
          "Task key '"
              + key.value()
              + "' is already used by another task "
              + "command in Menu#onOpen");
    }

    this.asyncCommand = new MenuAsyncCommand<>(key, operation, onStart, onSuccess, onFailure);
  }

  @Override
  public void repeat(MenuTaskKey key, MenuTaskSchedule schedule, MenuPeriodicTask<S> task) {
    this.ensureActive();
    this.ensureKeyDoesNotBelongToAsyncCommand(key);

    this.taskCommands.repeat(key, schedule, task);
  }

  @Override
  public void cancelTask(MenuTaskKey key) {
    this.ensureActive();
    this.ensureKeyDoesNotBelongToAsyncCommand(key);
    this.taskCommands.cancel(key);
  }

  public void finish() {
    if (this.finished) {
      throw new IllegalStateException("This menu open context has already finished");
    }

    this.finished = true;
  }

  public MenuAsyncCommand<S, ?> asyncCommand() {
    return this.asyncCommand;
  }

  public MenuTaskCommands<S> taskCommands() {
    return this.taskCommands;
  }

  private void ensureKeyDoesNotBelongToAsyncCommand(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");

    if (this.asyncCommand != null && this.asyncCommand.key().equals(key)) {
      throw new IllegalStateException(
          "Task key '"
              + key.value()
              + "' is already used by the asynchronous "
              + "command in Menu#onOpen");
    }
  }

  private void ensureActive() {
    if (this.finished) {
      throw new IllegalStateException(
          "A menu open context cannot be used " + "after Menu#onOpen has returned");
    }
  }
}
