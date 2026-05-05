package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

@Slf4j
@RequiredArgsConstructor
public final class SessionRenderer {
  @NonNull
  private final Plugin plugin;
  @NonNull
  private final SchedulerAdapter scheduler;
  @NonNull
  private final RenderEngine renderEngine;
  @NonNull
  private final MenuSessionState state;
  @NonNull
  private final ActiveSlotRegistry activeSlots;
  @NonNull
  private final PlayerResolver playerResolver;
  @NonNull
  private final ServerAccess serverAccess;

  public void refresh() {
    if (state.disposed()) return;
    if (!serverAccess.isPrimaryThread()) {
      scheduler.runSync(plugin, this::refresh);
      return;
    }
    if (playerResolver.resolveOnline(state.viewerId()) == null) return;

    try {
      var result = renderEngine.render(state.view(), state.definition(), state.currentPage());
      if (state.disposed()) return;
      state.currentPage(result.resolvedPage());
      activeSlots.replaceWith(result);
    } catch (Exception exception) {
      log.error("Render error in menu {}", state.definition().id(), exception);
    }
  }
}
