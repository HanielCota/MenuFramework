package com.hanielfialho.menuframework.internal.lifecycle;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.internal.error.MenuRuntimeLogger;
import com.hanielfialho.menuframework.internal.interaction.MenuOpenContextImpl;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.platform.MenuScheduler;
import com.hanielfialho.menuframework.internal.render.MenuFrame;
import com.hanielfialho.menuframework.internal.render.MenuFrameApplier;
import com.hanielfialho.menuframework.internal.render.MenuRenderer;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import com.hanielfialho.menuframework.internal.task.MenuAsyncCommand;
import com.hanielfialho.menuframework.internal.task.MenuTaskCommands;
import com.hanielfialho.menuframework.internal.task.MenuTaskRuntime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

/** Coordena abertura, atualização, navegação e encerramento de sessões. */
public final class MenuLifecycleCoordinator {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;
  private final MenuHistoryRegistry history;
  private final MenuScheduler scheduler;
  private final MenuFrameApplier frames;
  private final MenuTaskRuntime tasks;
  private final MenuRuntimeLogger logger;

  public MenuLifecycleCoordinator(
      MenuRuntimeState runtimeState,
      MenuSessionRegistry sessions,
      MenuHistoryRegistry history,
      MenuScheduler scheduler,
      MenuFrameApplier frames,
      MenuTaskRuntime tasks,
      MenuRuntimeLogger logger) {
    this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.history = Objects.requireNonNull(history, "history");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.frames = Objects.requireNonNull(frames, "frames");
    this.tasks = Objects.requireNonNull(tasks, "tasks");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public <S> boolean open(Player viewer, Menu<S> menu, S initialState) {
    if (!this.runtimeState.running()) {
      return false;
    }

    return this.scheduler.execute(
        viewer,
        () -> {
          try {
            this.openNow(viewer, menu, initialState, MenuOpenTransition.root());
          } catch (Exception throwable) {
            this.logger.reportOpenFailure(MenuFailureOperation.OPEN, viewer, menu, throwable);
          }
        });
  }

  public boolean refresh(Player viewer) {
    MenuSession<?> session = this.sessions.get(viewer.getUniqueId());

    if (!this.runtimeState.running()
        || session == null
        || !session.opened()
        || session.disposed()) {
      return false;
    }

    UUID sessionId = session.id();

    return this.scheduler.execute(viewer, () -> this.refreshIfCurrent(viewer, sessionId));
  }

  public boolean close(Player viewer) {
    MenuSession<?> session = this.sessions.get(viewer.getUniqueId());

    if (!this.runtimeState.running()
        || session == null
        || !session.opened()
        || session.disposed()) {
      return false;
    }

    UUID sessionId = session.id();

    return this.scheduler.execute(
        viewer, () -> this.closeIfCurrent(viewer, sessionId, MenuCloseReason.PLUGIN));
  }

  public boolean back(Player viewer) {
    MenuSession<?> session = this.sessions.get(viewer.getUniqueId());

    if (!this.runtimeState.running()
        || session == null
        || !session.opened()
        || session.disposed()
        || session.historyDepth() == 0) {
      return false;
    }

    UUID sessionId = session.id();

    return this.scheduler.execute(viewer, () -> this.openBackIfCurrent(viewer, sessionId));
  }

  public boolean isOpen(Player viewer) {
    MenuSession<?> session = this.sessions.get(viewer.getUniqueId());

    return this.runtimeState.running()
        && session != null
        && session.opened()
        && !session.disposed();
  }

  public int historyDepth(Player viewer) {
    MenuSession<?> session = this.sessions.get(viewer.getUniqueId());

    if (!this.runtimeState.running()
        || session == null
        || !session.opened()
        || session.disposed()) {
      return 0;
    }

    return session.historyDepth();
  }

  public void handleInventoryClose(Player viewer, UUID sessionId, MenuCloseReason reason) {
    this.disposeIfCurrent(viewer, sessionId, reason);
  }

  public void handleQuit(Player viewer) {
    UUID viewerId = viewer.getUniqueId();
    MenuSession<?> removed = this.sessions.remove(viewerId);

    this.history.clear(viewerId);

    if (removed != null) {
      this.terminate(removed, viewer, MenuCloseReason.QUIT);
    }
  }

  public void scheduleNavigation(
      Player viewer, UUID sourceSessionId, MenuNavigation<?> navigation) {
    this.scheduler.execute(
        viewer, () -> this.openNavigationIfCurrent(viewer, sourceSessionId, navigation));
  }

  public void scheduleBack(Player viewer, UUID sourceSessionId) {
    this.scheduler.execute(viewer, () -> this.openBackIfCurrent(viewer, sourceSessionId));
  }

  public void scheduleClose(Player viewer, UUID sessionId, MenuCloseReason reason) {
    this.scheduler.execute(viewer, () -> this.closeIfCurrent(viewer, sessionId, reason));
  }

  private <S> void openNow(
      Player viewer, Menu<S> menu, S initialState, MenuOpenTransition transition) {
    Objects.requireNonNull(transition, "transition");

    if (!this.runtimeState.running() || !viewer.isOnline()) {
      return;
    }

    int targetHistoryDepth = transition.targetDepth();

    MenuRenderContext<S> context =
        new MenuRenderContext<>(viewer, initialState, targetHistoryDepth);

    InteractionPolicy interactionPolicy =
        Objects.requireNonNull(
            menu.interactionPolicy(), "The menu returned a null interaction policy");

    Component title = Objects.requireNonNull(menu.title(context), "The menu returned a null title");

    MenuFrame<S> initialFrame = MenuRenderer.render(menu, viewer, initialState, targetHistoryDepth);

    UUID sessionId = UUID.randomUUID();
    MenuHolder holder =
        new MenuHolder(
            this.runtimeState.runtimeId(),
            sessionId,
            initialFrame.layout(),
            title,
            interactionPolicy);

    MenuSession<S> newSession =
        new MenuSession<>(
            sessionId,
            viewer.getUniqueId(),
            menu,
            holder,
            initialState,
            initialFrame,
            targetHistoryDepth);

    this.frames.applyInitialFrame(newSession.inventory(), initialFrame);

    UUID viewerId = viewer.getUniqueId();
    AtomicReference<MenuSession<?>> previousReference = new AtomicReference<>();
    AtomicBoolean publicationAttempted = new AtomicBoolean();

    /*
     * Publication is serialized with beginShutdown(). The monitor protects
     * only registry state; it is released before openInventory() executes.
     */
    boolean publicationAccepted =
        this.runtimeState.executeIfRunning(
            () -> {
              MenuSession<?> observedPrevious = this.sessions.get(viewerId);

              if (!transition.canCommit(observedPrevious, this.history, viewerId)) {
                return false;
              }

              MenuSession<?> replaced = this.sessions.publish(viewerId, newSession);

              previousReference.set(replaced);
              publicationAttempted.set(true);
              return replaced == observedPrevious;
            });

    if (!publicationAccepted) {
      if (publicationAttempted.get()) {
        this.rollbackFailedOpen(viewer, newSession, previousReference.get());
      } else {
        newSession.dispose();
      }

      return;
    }

    MenuSession<?> previous = previousReference.get();

    if (!this.runtimeState.running()) {
      this.rollbackFailedOpen(viewer, newSession, previous);
      return;
    }

    try {
      InventoryView openedView = viewer.openInventory(newSession.inventory());

      if (!this.runtimeState.running()) {
        this.rollbackFailedOpen(viewer, newSession, previous);
        return;
      }

      boolean validView =
          openedView != null
              && MenuViewAccess.belongsTo(openedView.getTopInventory(), sessionId)
              && this.sessions.get(viewerId) == newSession
              && MenuViewAccess.isSessionInventoryOpen(viewer, sessionId)
              && newSession.markOpened();

      if (!validView) {
        throw new IllegalStateException("The server did not open the expected menu inventory");
      }

      boolean historyCommitted =
          this.runtimeState.executeIfRunning(() -> transition.commit(this.history, viewerId));

      if (!historyCommitted) {
        throw new IllegalStateException("The menu history transition could not be committed");
      }

      if (!this.runtimeState.running()) {
        this.rollbackFailedOpen(viewer, newSession, previous);
        return;
      }
    } catch (Exception throwable) {
      this.rollbackFailedOpen(viewer, newSession, previous);
      throw throwable;
    }

    if (previous != null) {
      this.terminate(previous, viewer, transition.previousCloseReason());
    }

    if (this.isUsable(newSession, viewer)) {
      this.notifyOpen(newSession, viewer);
    }
  }

  private void refreshIfCurrent(Player viewer, UUID sessionId) {
    if (!this.runtimeState.running()) {
      return;
    }

    MenuSession<?> session = this.sessions.current(viewer.getUniqueId(), sessionId);

    if (session == null || !MenuViewAccess.isSessionInventoryOpen(viewer, sessionId)) {
      return;
    }

    this.refreshTyped(session, viewer);
  }

  private <S> void refreshTyped(MenuSession<S> session, Player viewer) {
    try {
      this.frames.renderAndApply(session, viewer, session.state());
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.REFRESH_RENDER, session, throwable);
    }
  }

  private void openNavigationIfCurrent(
      Player viewer, UUID sourceSessionId, MenuNavigation<?> navigation) {
    if (!this.runtimeState.running()) {
      return;
    }

    MenuSession<?> source = this.sessions.current(viewer.getUniqueId(), sourceSessionId);

    if (source == null || !MenuViewAccess.isSessionInventoryOpen(viewer, sourceSessionId)) {
      return;
    }

    try {
      MenuOpenTransition transition = MenuOpenTransition.forward(source, this.history);

      this.openNavigationTyped(viewer, navigation, transition);
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.NAVIGATION, source, throwable);
    }
  }

  private <T> void openNavigationTyped(
      Player viewer, MenuNavigation<T> navigation, MenuOpenTransition transition) {
    try {
      this.openNow(viewer, navigation.menu(), navigation.initialState(), transition);
    } catch (Exception throwable) {
      this.logger.reportOpenFailure(
          MenuFailureOperation.OPEN, viewer, navigation.menu(), throwable);
    }
  }

  private void openBackIfCurrent(Player viewer, UUID sourceSessionId) {
    if (!this.runtimeState.running()) {
      return;
    }

    MenuSession<?> source = this.sessions.current(viewer.getUniqueId(), sourceSessionId);

    if (source == null || !MenuViewAccess.isSessionInventoryOpen(viewer, sourceSessionId)) {
      return;
    }

    try {
      MenuOpenTransition transition = MenuOpenTransition.back(source, this.history);

      this.openHistoryEntry(viewer, transition.backTarget(), transition);
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.BACK_NAVIGATION, source, throwable);
    }
  }

  private void openHistoryEntry(
      Player viewer, MenuHistoryEntry<?> entry, MenuOpenTransition transition) {
    this.openHistoryEntryTyped(viewer, entry, transition);
  }

  private <S> void openHistoryEntryTyped(
      Player viewer, MenuHistoryEntry<S> entry, MenuOpenTransition transition) {
    try {
      this.openNow(viewer, entry.menu(), entry.state(), transition);
    } catch (Exception throwable) {
      this.logger.reportOpenFailure(MenuFailureOperation.OPEN, viewer, entry.menu(), throwable);
    }
  }

  private void closeIfCurrent(Player viewer, UUID sessionId, MenuCloseReason reason) {
    if (!this.runtimeState.running()) {
      return;
    }

    UUID viewerId = viewer.getUniqueId();
    MenuSession<?> session = this.sessions.current(viewerId, sessionId);

    if (session == null) {
      return;
    }

    if (!this.sessions.remove(viewerId, session)) {
      return;
    }

    this.history.clear(viewerId);

    try {
      if (MenuViewAccess.isSessionInventoryOpen(viewer, sessionId)) {
        viewer.closeInventory();
      }
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.INVENTORY_CLOSE, session, throwable);
    } finally {
      this.terminate(session, viewer, reason);
    }
  }

  private void disposeIfCurrent(Player viewer, UUID sessionId, MenuCloseReason reason) {
    if (!this.runtimeState.running()) {
      return;
    }

    UUID viewerId = viewer.getUniqueId();
    MenuSession<?> session = this.sessions.current(viewerId, sessionId);

    if (session == null) {
      return;
    }

    if (!this.sessions.remove(viewerId, session)) {
      return;
    }

    this.history.clear(viewerId);
    this.terminate(session, viewer, reason);
  }

  private void rollbackFailedOpen(
      Player viewer, MenuSession<?> failedSession, MenuSession<?> previous) {
    UUID viewerId = viewer.getUniqueId();
    boolean previousRestored = false;

    if (this.runtimeState.running()
        && previous != null
        && previous.opened()
        && !previous.disposed()
        && MenuViewAccess.isSessionInventoryOpen(viewer, previous.id())) {
      previousRestored = this.sessions.restore(viewerId, failedSession, previous);
    }

    if (!previousRestored) {
      this.sessions.remove(viewerId, failedSession);
      this.history.clear(viewerId);
    }

    try {
      if (MenuViewAccess.isSessionInventoryOpen(viewer, failedSession.id())) {
        viewer.closeInventory();
      }
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(
          MenuFailureOperation.FAILED_OPEN_CLEANUP, failedSession, throwable);
    }

    this.terminate(failedSession, viewer, MenuCloseReason.OPEN_FAILED);

    if (previous != null && !previousRestored) {
      this.terminate(previous, viewer, MenuCloseReason.OPEN_FAILED);
    }
  }

  private void notifyOpen(MenuSession<?> session, Player viewer) {
    this.notifyOpenTyped(session, viewer);
  }

  private <S> void notifyOpenTyped(MenuSession<S> session, Player viewer) {
    if (!this.runtimeState.running() || !session.claimOpenCallback()) {
      return;
    }

    MenuOpenContextImpl<S> context =
        new MenuOpenContextImpl<>(
            session.id(), viewer, session.state(), session.revision(), session.historyDepth());

    boolean successful = false;

    try {
      session.menu().onOpen(context);
      successful = true;
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.OPEN_CALLBACK, session, throwable);
    } finally {
      context.finish();
    }

    if (!successful || !this.isUsable(session, viewer)) {
      return;
    }

    MenuTaskCommands<S> taskCommands = context.taskCommands();
    MenuAsyncCommand<S, ?> asyncCommand = context.asyncCommand();

    boolean asyncStarted =
        asyncCommand == null || this.tasks.startAsync(session, viewer, asyncCommand);

    if (!asyncStarted || !this.isUsable(session, viewer)) {
      return;
    }

    /*
     * Cancelamentos explícitos são efetivados somente depois que a
     * transição de estado principal foi aplicada. Isso evita efeitos
     * parciais quando o loading render ou a criação da task falha.
     */
    this.tasks.applyCancellations(session, taskCommands);

    if (!this.isUsable(session, viewer)) {
      return;
    }

    this.tasks.startPeriodic(session, viewer, taskCommands);
  }

  private boolean isUsable(MenuSession<?> session, Player viewer) {
    return this.runtimeState.running()
        && this.sessions.isCurrent(session)
        && MenuViewAccess.isSessionInventoryOpen(viewer, session.id());
  }

  private void terminate(MenuSession<?> session, Player viewer, MenuCloseReason reason) {
    this.terminateTyped(session, viewer, reason);
  }

  private <S> void terminateTyped(MenuSession<S> session, Player viewer, MenuCloseReason reason) {
    boolean notifyClose = session.lifecycleStarted();
    boolean disposed = session.dispose();

    this.sessions.untrack(session);

    if (!disposed) {
      return;
    }

    if (!notifyClose) {
      return;
    }

    try {
      session.menu().onClose(session.context(viewer), reason);
    } catch (Exception throwable) {
      this.logger.reportSessionFailure(MenuFailureOperation.CLOSE_CALLBACK, session, throwable);
    }
  }
}
