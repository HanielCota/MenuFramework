package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SessionLifecycle {
  private static final Logger log =
      Logger.getLogger(SessionLifecycle.class.getName());
  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final MenuSessionState state;
  @NonNull private final ActiveSlotRegistry activeSlots;
  @NonNull private final ServerAccess serverAccess;
  private final AtomicReference<Object> refreshTaskHandle = new AtomicReference<>();
  private @Nullable MenuSession session;

  public SessionLifecycle(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @Nullable MenuSession session,
      @NonNull MenuSessionState state,
      @NonNull ActiveSlotRegistry activeSlots,
      @NonNull ServerAccess serverAccess) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.session = session;
    this.state = Objects.requireNonNull(state, "state");
    this.activeSlots = Objects.requireNonNull(activeSlots, "activeSlots");
    this.serverAccess = Objects.requireNonNull(serverAccess, "serverAccess");
  }

  public void setSession(@NonNull MenuSession session) {
    this.session = Objects.requireNonNull(session, "session");
  }

  public void setRefreshTaskHandle(@NonNull Object refreshTaskHandle) {
    this.refreshTaskHandle.set(Objects.requireNonNull(refreshTaskHandle, "refreshTaskHandle"));
  }

  public @NonNull CompletableFuture<Void> dispose() {
    var future = new CompletableFuture<Void>();
    if (state.disposed()) {
      future.complete(null);
      return future;
    }
    scheduler.runSync(plugin, () -> completeDisposal(future));
    return future;
  }

  public void disposeImmediately() {
    if (serverAccess.isNotPrimaryThread()) {
      scheduler.runSync(plugin, this::disposeImmediately);
      return;
    }
    if (!state.markDisposed()) return;

    cancelRefreshTask();
    closePlayerInventory();
    fireCloseFeatures();
    activeSlots.clear();
  }

  private void completeDisposal(@NonNull CompletableFuture<Void> future) {
    try {
      disposeImmediately();
    } catch (Exception exception) {
      future.completeExceptionally(exception);
      return;
    }
    future.complete(null);
  }

  private void cancelRefreshTask() {
    var taskHandle = refreshTaskHandle.getAndSet(null);
    if (taskHandle == null) return;
    scheduler.cancel(taskHandle);
  }

  private void closePlayerInventory() {
    var playerOpt = serverAccess.findOnlinePlayer(state.viewerId());
    if (playerOpt.isEmpty()) return;
    var player = playerOpt.get();
    if (!player.getOpenInventory().getTopInventory().equals(state.view().getTopInventory())) return;
    player.closeInventory();
  }

  private void fireCloseFeatures() {
    var session = this.session;
    if (session == null) return;
    for (var feature : state.definition().features()) {
      try {
        feature.onClose(session);
      } catch (Exception exception) {
        log.log(
            Level.WARNING,
            exception,
            () ->
                "menu.feature.onClose_failed menuId=%s featureType=%s"
                    .formatted(state.definition().id(), feature.getClass().getSimpleName()));
      }
    }
  }
}
