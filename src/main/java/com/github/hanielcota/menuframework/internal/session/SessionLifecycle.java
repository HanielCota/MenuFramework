package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Slf4j
public final class SessionLifecycle {
  @NonNull
  private final Plugin plugin;
  @NonNull
  private final SchedulerAdapter scheduler;
  @NonNull
  private final MenuSessionState state;
  @NonNull
  private final ActiveSlotRegistry activeSlots;
  @NonNull
  private final ServerAccess serverAccess;
  private @Nullable MenuSession session;
  private @Nullable Object refreshTaskHandle;

  public SessionLifecycle(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull MenuSession session,
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
    this.refreshTaskHandle = Objects.requireNonNull(refreshTaskHandle, "refreshTaskHandle");
  }

  public @NonNull CompletableFuture<Void> dispose() {
    var future = new CompletableFuture<Void>();
    scheduler.runSync(plugin, () -> completeDisposal(future));
    return future;
  }

  public void disposeImmediately() {
    if (!serverAccess.isPrimaryThread()) {
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
      future.complete(null);
    } catch (Exception exception) {
      future.completeExceptionally(exception);
    }
  }

  private void cancelRefreshTask() {
    var taskHandle = refreshTaskHandle;
    if (taskHandle == null) return;
    scheduler.cancel(taskHandle);
    refreshTaskHandle = null;
  }

  private void closePlayerInventory() {
    var player = serverAccess.findOnlinePlayer(state.viewerId()).orElse(null);
    if (player == null) return;
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
        log.warn(
            "menu.feature.onClose_failed menuId={} featureType={}",
            state.definition().id(),
            feature.getClass().getSimpleName(),
            exception);
      }
    }
  }
}
