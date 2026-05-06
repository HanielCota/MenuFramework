package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.RefreshingMenuFeature;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class RefreshScheduler {

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final ServerAccess serverAccess;

  public RefreshScheduler(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull ServerAccess serverAccess) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.serverAccess = serverAccess;
  }

  private static long resolveIntervalTicks(@NonNull Iterable<MenuFeature> features) {
    long interval = -1;
    for (var feature : features) {
      if (feature instanceof RefreshingMenuFeature refreshingFeature) {
        var ticks = refreshingFeature.refreshIntervalTicks();
        if (ticks <= 0) {
          throw new IllegalArgumentException("Refresh interval must be positive, got: " + ticks);
        }
        interval = interval < 0 ? ticks : Math.min(interval, ticks);
      }
    }
    return interval;
  }

  public @Nullable Object schedule(@NonNull MenuSessionImpl session) {
    var intervalTicks = resolveIntervalTicks(session.features());
    if (intervalTicks <= 0) return null;

    var sessionRef = new java.lang.ref.WeakReference<>(session);
    return scheduler.runSyncRepeating(
        plugin, () -> refreshTick(sessionRef), intervalTicks, intervalTicks);
  }

  private void refreshTick(java.lang.ref.WeakReference<MenuSessionImpl> sessionRef) {
    var session = sessionRef.get();
    if (session == null || session.disposed()) return;
    var viewer = serverAccess.findOnlinePlayer(session.viewerId()).orElse(null);
    if (viewer == null || !viewer.isOnline()) return;
    var openInventory = viewer.getOpenInventory();
    if (!openInventory.equals(session.view())) return;

    session.refresh();

    for (var feature : session.features()) {
      feature.onTick(session, viewer);
    }
  }
}
