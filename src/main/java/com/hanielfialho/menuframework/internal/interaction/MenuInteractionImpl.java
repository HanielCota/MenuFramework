package com.hanielfialho.menuframework.internal.interaction;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.feedback.MenuFeedbackSignal;
import com.hanielfialho.menuframework.api.task.MenuPeriodicTask;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import com.hanielfialho.menuframework.internal.lifecycle.MenuNavigation;
import com.hanielfialho.menuframework.internal.task.MenuAsyncCommand;
import com.hanielfialho.menuframework.internal.task.MenuTaskCommands;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Buffer transacional interno dos comandos produzidos por um clique.
 *
 * @param <S> tipo de estado da sessão
 */
public final class MenuInteractionImpl<S> implements MenuInteraction<S> {

  private enum TerminalCommand {
    NONE,
    CLOSE,
    OPEN,
    BACK
  }

  private final UUID sessionId;
  private final Player viewer;
  private final MenuClick click;
  private final long revision;
  private final int historyDepth;
  private final MenuTaskCommands<S> taskCommands;
  private final List<MenuFeedbackSignal> feedbackSignals;

  private S resultingState;
  private boolean refreshRequested;
  private TerminalCommand terminalCommand;
  private MenuNavigation<?> navigationRequest;
  private MenuAsyncCommand<S, ?> asyncCommand;
  private boolean finished;

  public MenuInteractionImpl(
      UUID sessionId,
      Player viewer,
      S initialState,
      long revision,
      int historyDepth,
      MenuClick click) {
    this.sessionId = Objects.requireNonNull(sessionId, "sessionId");

    this.viewer = Objects.requireNonNull(viewer, "viewer");

    this.resultingState = Objects.requireNonNull(initialState, "initialState");

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }

    this.revision = revision;

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }

    this.historyDepth = historyDepth;

    this.click = Objects.requireNonNull(click, "click");

    this.taskCommands = new MenuTaskCommands<>();
    this.feedbackSignals = new ArrayList<>();
    this.terminalCommand = TerminalCommand.NONE;
  }

  public MenuInteractionImpl(
      UUID sessionId, Player viewer, S initialState, long revision, MenuClick click) {
    this(sessionId, viewer, initialState, revision, 0, click);
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
    return this.resultingState;
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
  public MenuClick click() {
    this.ensureActive();
    return this.click;
  }

  @Override
  public void setState(S newState) {
    this.ensureStateCommandAllowed();

    this.resultingState = Objects.requireNonNull(newState, "newState");

    this.refreshRequested = true;
  }

  @Override
  public void refresh() {
    this.ensureStateCommandAllowed();
    this.refreshRequested = true;
  }

  @Override
  public void feedback(MenuFeedbackSignal signal) {
    this.ensureActive();
    this.feedbackSignals.add(Objects.requireNonNull(signal, "signal"));
  }

  @Override
  public void close() {
    this.ensureTerminalCommandAllowed();

    this.terminalCommand = TerminalCommand.CLOSE;
    this.refreshRequested = false;
  }

  @Override
  public <T> void open(Menu<T> menu, T initialState) {
    this.ensureTerminalCommandAllowed();

    this.navigationRequest = new MenuNavigation<>(menu, initialState);

    this.terminalCommand = TerminalCommand.OPEN;
    this.refreshRequested = false;
  }

  @Override
  public void back() {
    this.ensureTerminalCommandAllowed();

    if (this.historyDepth == 0) {
      throw new IllegalStateException("The current menu has no previous history entry");
    }

    this.terminalCommand = TerminalCommand.BACK;
    this.refreshRequested = false;
  }

  @Override
  public <R> void executeAsync(
      MenuTaskKey key,
      Operation<R> operation,
      Start<S> onStart,
      Success<S, R> onSuccess,
      Failure<S> onFailure) {
    this.ensureActive();
    this.ensureNoTerminalCommand();
    Objects.requireNonNull(key, "key");

    if (this.refreshRequested) {
      throw new IllegalStateException(
          "An asynchronous command cannot be combined "
              + "with setState() or refresh() in "
              + "the same interaction");
    }

    if (this.asyncCommand != null) {
      throw new IllegalStateException(
          "Only one asynchronous command can be " + "requested per interaction");
    }

    if (this.taskCommands.contains(key)) {
      throw new IllegalStateException(
          "Task key '"
              + key.value()
              + "' is already used by another task "
              + "command in this interaction");
    }

    this.asyncCommand = new MenuAsyncCommand<>(key, operation, onStart, onSuccess, onFailure);
  }

  @Override
  public void repeat(MenuTaskKey key, MenuTaskSchedule schedule, MenuPeriodicTask<S> task) {
    this.ensureActive();
    this.ensureNoTerminalCommand();
    this.ensureKeyDoesNotBelongToAsyncCommand(key);

    this.taskCommands.repeat(key, schedule, task);
  }

  @Override
  public void cancelTask(MenuTaskKey key) {
    this.ensureActive();
    this.ensureNoTerminalCommand();
    this.ensureKeyDoesNotBelongToAsyncCommand(key);
    this.taskCommands.cancel(key);
  }

  public void finish() {
    if (this.finished) {
      throw new IllegalStateException("This menu interaction has already finished");
    }

    this.finished = true;
  }

  public S resultingState() {
    return this.resultingState;
  }

  public boolean refreshRequested() {
    return this.refreshRequested;
  }

  public boolean closeRequested() {
    return this.terminalCommand == TerminalCommand.CLOSE;
  }

  public boolean backRequested() {
    return this.terminalCommand == TerminalCommand.BACK;
  }

  public MenuNavigation<?> navigationRequest() {
    return this.navigationRequest;
  }

  public MenuAsyncCommand<S, ?> asyncCommand() {
    return this.asyncCommand;
  }

  public MenuTaskCommands<S> taskCommands() {
    return this.taskCommands;
  }

  public List<MenuFeedbackSignal> feedbackSignals() {
    return List.copyOf(this.feedbackSignals);
  }

  private void ensureTerminalCommandAllowed() {
    this.ensureActive();
    this.ensureNoTerminalCommand();
    this.ensureNoAsyncCommand();

    if (this.refreshRequested || !this.taskCommands.isEmpty()) {
      throw new IllegalStateException(
          "A terminal command cannot be combined with state "
              + "or task commands in the same interaction");
    }
  }

  private void ensureStateCommandAllowed() {
    this.ensureActive();
    this.ensureNoTerminalCommand();
    this.ensureNoAsyncCommand();
  }

  private void ensureNoTerminalCommand() {
    if (this.terminalCommand != TerminalCommand.NONE) {
      throw new IllegalStateException(
          "A terminal menu command has already " + "been requested: " + this.terminalCommand);
    }
  }

  private void ensureNoAsyncCommand() {
    if (this.asyncCommand != null) {
      throw new IllegalStateException("An asynchronous command has already " + "been requested");
    }
  }

  private void ensureKeyDoesNotBelongToAsyncCommand(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");

    if (this.asyncCommand != null && this.asyncCommand.key().equals(key)) {
      throw new IllegalStateException(
          "Task key '"
              + key.value()
              + "' is already used by the asynchronous "
              + "command in this interaction");
    }
  }

  private void ensureActive() {
    if (this.finished) {
      throw new IllegalStateException(
          "A menu interaction cannot be used " + "after its handler has returned");
    }
  }
}
