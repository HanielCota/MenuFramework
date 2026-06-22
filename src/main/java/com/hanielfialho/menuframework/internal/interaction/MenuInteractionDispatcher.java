package com.hanielfialho.menuframework.internal.interaction;

import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.internal.error.MenuRuntimeLogger;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.lifecycle.MenuLifecycleCoordinator;
import com.hanielfialho.menuframework.internal.lifecycle.MenuNavigation;
import com.hanielfialho.menuframework.internal.render.MenuFrameApplier;
import com.hanielfialho.menuframework.internal.render.MenuSlot;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import com.hanielfialho.menuframework.internal.task.MenuAsyncCommand;
import com.hanielfialho.menuframework.internal.task.MenuTaskCommands;
import com.hanielfialho.menuframework.internal.task.MenuTaskRuntime;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/** Executa handlers de botão e interpreta o buffer transacional produzido por MenuInteraction. */
public final class MenuInteractionDispatcher {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;
  private final MenuLifecycleCoordinator lifecycle;
  private final MenuFrameApplier frames;
  private final MenuTaskRuntime tasks;
  private final MenuRuntimeLogger logger;

  public MenuInteractionDispatcher(
      MenuRuntimeState runtimeState,
      MenuSessionRegistry sessions,
      MenuLifecycleCoordinator lifecycle,
      MenuFrameApplier frames,
      MenuTaskRuntime tasks,
      MenuRuntimeLogger logger) {
    this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    this.frames = Objects.requireNonNull(frames, "frames");
    this.tasks = Objects.requireNonNull(tasks, "tasks");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public void dispatch(Player viewer, UUID sessionId, MenuClick click) {
    if (!this.runtimeState.running()) {
      return;
    }

    MenuSession<?> session = this.sessions.current(viewer.getUniqueId(), sessionId);

    if (session == null || !MenuViewAccess.isSessionInventoryOpen(viewer, sessionId)) {
      return;
    }

    this.dispatchTyped(session, viewer, click);
  }

  private <S> void dispatchTyped(MenuSession<S> session, Player viewer, MenuClick click) {
    if (!session.layout().contains(click.rawSlot())) {
      return;
    }

    MenuSlot<S> menuSlot = session.frame().slotOrNull(click.rawSlot());

    if (menuSlot == null || !menuSlot.clickable()) {
      return;
    }

    MenuClickHandler<S> clickHandler = menuSlot.clickHandler().orElse(null);

    if (clickHandler == null) {
      return;
    }

    MenuInteractionImpl<S> interaction =
        new MenuInteractionImpl<>(
            session.id(),
            viewer,
            session.state(),
            session.revision(),
            session.historyDepth(),
            click);

    boolean successful = false;

    try {
      clickHandler.handle(interaction);
      successful = true;
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.CLICK_HANDLER, session, throwable);
    } finally {
      interaction.finish();
    }

    if (!successful || !this.runtimeState.running() || !this.sessions.isCurrent(session)) {
      return;
    }

    if (interaction.closeRequested()) {
      this.lifecycle.scheduleClose(viewer, session.id(), MenuCloseReason.BUTTON);
      return;
    }

    if (interaction.backRequested()) {
      this.lifecycle.scheduleBack(viewer, session.id());
      return;
    }

    MenuNavigation<?> navigation = interaction.navigationRequest();

    if (navigation != null) {
      this.lifecycle.scheduleNavigation(viewer, session.id(), navigation);
      return;
    }

    MenuTaskCommands<S> taskCommands = interaction.taskCommands();
    MenuAsyncCommand<S, ?> asyncCommand = interaction.asyncCommand();
    boolean stateCommandApplied = true;

    if (asyncCommand != null) {
      stateCommandApplied = this.tasks.startAsync(session, viewer, asyncCommand);
    }

    if (stateCommandApplied && asyncCommand == null && interaction.refreshRequested()) {
      try {
        stateCommandApplied =
            this.frames.renderAndApply(session, viewer, interaction.resultingState());
      } catch (Exception throwable) {
        this.logger.reportSessionFailure(MenuFailureOperation.CLICK_RENDER, session, throwable);

        stateCommandApplied = false;
      }
    }

    if (!stateCommandApplied || this.isUnusable(session, viewer)) {
      return;
    }

    /*
     * Só efetivamos cancelamentos explícitos depois que a transição de
     * estado principal foi aplicada. Uma falha de render ou de criação da
     * task não pode cancelar operações anteriores parcialmente.
     */
    this.tasks.applyCancellations(session, taskCommands);

    if (this.isUnusable(session, viewer)) {
      return;
    }

    this.tasks.startPeriodic(session, viewer, taskCommands);
  }

  private boolean isUnusable(MenuSession<?> session, Player viewer) {
    return !this.runtimeState.running()
        || !this.sessions.isCurrent(session)
        || !MenuViewAccess.isSessionInventoryOpen(viewer, session.id());
  }
}
