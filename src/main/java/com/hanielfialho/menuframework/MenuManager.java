package com.hanielfialho.menuframework;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.api.error.DefaultMenuErrorHandler;
import com.hanielfialho.menuframework.api.error.MenuErrorHandler;
import com.hanielfialho.menuframework.internal.error.MenuRuntimeLogger;
import com.hanielfialho.menuframework.internal.interaction.MenuInteractionDispatcher;
import com.hanielfialho.menuframework.internal.inventory.MenuEventHandler;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import com.hanielfialho.menuframework.internal.lifecycle.MenuHistoryRegistry;
import com.hanielfialho.menuframework.internal.lifecycle.MenuLifecycleCoordinator;
import com.hanielfialho.menuframework.internal.platform.MenuScheduler;
import com.hanielfialho.menuframework.internal.render.MenuFrameApplier;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import com.hanielfialho.menuframework.internal.task.MenuAsyncTaskRuntime;
import com.hanielfialho.menuframework.internal.task.MenuPeriodicTaskRuntime;
import com.hanielfialho.menuframework.internal.task.MenuTaskRuntime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Public facade for opening, refreshing, closing and navigating menus.
 *
 * <p>Instances are created by {@link MenuFramework}; the constructor is not public so a manager
 * cannot accidentally be used without its listener. Menu operations that mutate an inventory are
 * submitted to the viewer's Paper entity scheduler. Their boolean return value therefore reports
 * whether the request was accepted, not whether the later operation completed successfully.
 */
public final class MenuManager {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;
  private final MenuHistoryRegistry history;
  private final MenuLifecycleCoordinator lifecycle;
  private final MenuInteractionDispatcher interactions;
  private final MenuEventHandler eventHandler;

  MenuManager(Plugin plugin) {
    this(plugin, MenuFrameworkConfiguration.defaults());
  }

  MenuManager(Plugin plugin, MenuFrameworkConfiguration configuration) {
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(configuration, "configuration");

    this.runtimeState = new MenuRuntimeState();
    this.sessions = new MenuSessionRegistry();
    this.history = new MenuHistoryRegistry(configuration.maxNavigationHistoryDepth());

    MenuErrorHandler errorHandler =
        configuration.errorHandler().orElseGet(() -> new DefaultMenuErrorHandler(plugin));

    MenuRuntimeLogger logger = new MenuRuntimeLogger(plugin, errorHandler);
    MenuScheduler scheduler = new MenuScheduler(plugin, this.runtimeState);
    MenuFrameApplier frames = new MenuFrameApplier(this.runtimeState, this.sessions);
    MenuAsyncTaskRuntime asyncTasks =
        new MenuAsyncTaskRuntime(this.runtimeState, this.sessions, scheduler, frames, logger);
    MenuPeriodicTaskRuntime periodicTasks =
        new MenuPeriodicTaskRuntime(this.runtimeState, this.sessions, scheduler, frames, logger);
    MenuTaskRuntime tasks = new MenuTaskRuntime(asyncTasks, periodicTasks);

    this.lifecycle =
        new MenuLifecycleCoordinator(
            this.runtimeState,
            this.sessions,
            this.history,
            scheduler,
            frames,
            tasks,
            logger,
            configuration.defaultTheme(),
            configuration.defaultFeedback());
    this.interactions =
        new MenuInteractionDispatcher(
            this.runtimeState, this.sessions, this.lifecycle, frames, tasks, logger);

    this.eventHandler =
        new MenuEventHandler() {
          @Override
          public boolean owns(MenuHolder holder) {
            return MenuManager.this.owns(holder);
          }

          @Override
          public void dispatchClick(Player viewer, UUID sessionId, MenuClick click) {
            MenuManager.this.dispatchClick(viewer, sessionId, click);
          }

          @Override
          public void handleInventoryClose(Player viewer, UUID sessionId, MenuCloseReason reason) {
            MenuManager.this.handleInventoryClose(viewer, sessionId, reason);
          }

          @Override
          public void handleQuit(Player viewer) {
            MenuManager.this.handleQuit(viewer);
          }
        };
  }

  /**
   * Invalidates the runtime and cancels all session-owned tasks.
   *
   * <p>This method is idempotent and intended for {@code JavaPlugin#onDisable()}. It does not
   * access player inventory views or invoke menu lifecycle callbacks because plugin shutdown does
   * not have a guaranteed entity-region context on Folia.
   */
  public void shutdown() {
    if (!this.runtimeState.beginShutdown()) {
      return;
    }

    List<MenuSession<?>> snapshot = this.sessions.clearAndSnapshotLiveSessions();
    this.history.clearAll();

    for (MenuSession<?> session : snapshot) {
      session.dispose();
    }
  }

  /**
   * Returns whether this manager has been permanently shut down.
   *
   * @return {@code true} after the first call to {@link #shutdown()}
   */
  public boolean isShutdown() {
    return this.runtimeState.isShutdown();
  }

  /**
   * Submits a request to open a menu for a player.
   *
   * <p>The menu definition may be reused by many viewers, but {@code initialState} must be
   * immutable or treated as immutable. Opening a root menu clears that viewer's navigation history.
   *
   * @param viewer player that will own the session
   * @param menu reusable menu definition
   * @param initialState non-null initial state
   * @param <S> state type
   * @return {@code true} when the entity scheduler accepted the request
   * @throws NullPointerException if an argument is {@code null}
   */
  public <S> boolean open(Player viewer, Menu<S> menu, S initialState) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(menu, "menu");
    Objects.requireNonNull(initialState, "initialState");
    return this.lifecycle.open(viewer, menu, initialState);
  }

  /**
   * Submits a refresh of the viewer's current session.
   *
   * @param viewer session owner
   * @return {@code true} when a current session existed and scheduling was accepted
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public boolean refresh(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return this.lifecycle.refresh(viewer);
  }

  /**
   * Submits a programmatic close using {@link MenuCloseReason#PLUGIN}.
   *
   * @param viewer session owner
   * @return {@code true} when a current session existed and scheduling was accepted
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public boolean close(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return this.lifecycle.close(viewer);
  }

  /**
   * Returns whether the runtime currently tracks an open session.
   *
   * <p>This is a concurrent registry snapshot; it intentionally does not inspect Bukkit's current
   * {@code InventoryView} from the calling thread.
   *
   * @param viewer viewer to inspect
   * @return tracked-open flag
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public boolean isOpen(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return this.lifecycle.isOpen(viewer);
  }

  /**
   * Returns the number of history entries restorable by {@link #back(Player)}.
   *
   * @param viewer viewer to inspect
   * @return current history depth, or zero when no session is open
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public int historyDepth(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return this.lifecycle.historyDepth(viewer);
  }

  /**
   * Returns whether the current menu has a previous history entry.
   *
   * @param viewer viewer to inspect
   * @return {@code true} when {@link #back(Player)} can be requested
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public boolean canGoBack(Player viewer) {
    return this.historyDepth(viewer) > 0;
  }

  /**
   * Submits restoration of the previous menu and its state snapshot.
   *
   * @param viewer session owner
   * @return {@code true} when a back transition existed and scheduling was accepted
   * @throws NullPointerException if {@code viewer} is {@code null}
   */
  public boolean back(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return this.lifecycle.back(viewer);
  }

  MenuEventHandler eventHandler() {
    return this.eventHandler;
  }

  boolean owns(MenuHolder holder) {
    return this.runtimeState.runtimeId().equals(holder.runtimeId());
  }

  void dispatchClick(Player viewer, UUID sessionId, MenuClick click) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(click, "click");
    this.interactions.dispatch(viewer, sessionId, click);
  }

  void handleInventoryClose(Player viewer, UUID sessionId, MenuCloseReason reason) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(reason, "reason");
    this.lifecycle.handleInventoryClose(viewer, sessionId, reason);
  }

  void handleQuit(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");
    this.lifecycle.handleQuit(viewer);
  }
}
