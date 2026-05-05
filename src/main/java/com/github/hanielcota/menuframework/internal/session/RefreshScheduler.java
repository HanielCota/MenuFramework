package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.RefreshingMenuFeature;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public final class RefreshScheduler {

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final ServerAccess serverAccess;

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

    return scheduler.runSyncRepeating(
        plugin, () -> refreshTick(session), intervalTicks, intervalTicks);
  }

  private void refreshTick(@NonNull MenuSessionImpl session) {
    if (session.disposed()) return;
    var viewer = serverAccess.findOnlinePlayer(session.viewerId()).orElse(null);
    if (viewer == null || !viewer.isOnline()) return;
    if (session.view().getTopInventory().getViewers().isEmpty()) return;

    session.refresh();

    for (var feature : session.features()) {
      feature.onTick(session, viewer);
    }
  }
}
