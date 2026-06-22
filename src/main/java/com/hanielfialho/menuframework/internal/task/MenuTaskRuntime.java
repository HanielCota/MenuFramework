package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import java.util.Objects;
import org.bukkit.entity.Player;

/** Fachada interna para os dois tipos de task pertencentes à sessão. */
public final class MenuTaskRuntime {

  private final MenuAsyncTaskRuntime asyncTasks;
  private final MenuPeriodicTaskRuntime periodicTasks;

  public MenuTaskRuntime(MenuAsyncTaskRuntime asyncTasks, MenuPeriodicTaskRuntime periodicTasks) {
    this.asyncTasks = Objects.requireNonNull(asyncTasks, "asyncTasks");
    this.periodicTasks = Objects.requireNonNull(periodicTasks, "periodicTasks");
  }

  public <S> void applyCancellations(MenuSession<S> session, MenuTaskCommands<S> commands) {
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(commands, "commands");

    for (MenuTaskKey key : commands.cancellations()) {
      session.cancelTask(key);
    }
  }

  public <S, R> boolean startAsync(
      MenuSession<S> session, Player viewer, MenuAsyncCommand<S, R> command) {
    return this.asyncTasks.start(session, viewer, command);
  }

  public <S> void startPeriodic(
      MenuSession<S> session, Player viewer, MenuTaskCommands<S> commands) {
    Objects.requireNonNull(commands, "commands");

    this.periodicTasks.startAll(session, viewer, commands.periodicCommands());
  }
}
