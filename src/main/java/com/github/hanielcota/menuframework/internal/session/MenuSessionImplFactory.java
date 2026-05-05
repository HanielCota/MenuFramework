package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.internal.interaction.ClickExecutor;
import com.github.hanielcota.menuframework.internal.interaction.InteractionPolicy;
import com.github.hanielcota.menuframework.internal.interaction.MenuInteractionController;
import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class MenuSessionImplFactory {

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final ServerAccess serverAccess;
  @NonNull private final RenderEngine renderEngine;
  @NonNull private final MenuService menuService;
  @NonNull private final ClickExecutor clickExecutor;

  public @NonNull MenuSessionImpl create(
      @NonNull UUID viewerId, @NonNull MenuDefinition definition, @NonNull InventoryView view) {
    var state = new MenuSessionState(viewerId, definition, view);
    var activeSlots = new ActiveSlotRegistry();
    var playerResolver = new PlayerResolver(menuService, serverAccess);

    var renderer =
        new SessionRenderer(
            plugin, scheduler, renderEngine, state, activeSlots, playerResolver, serverAccess);
    var lifecycle = new SessionLifecycle(plugin, scheduler, null, state, activeSlots, serverAccess);
    var interactions =
        new MenuInteractionController(
            state, activeSlots, playerResolver, clickExecutor, new InteractionPolicy());

    var session = new MenuSessionImpl(state, renderer, interactions, lifecycle);
    lifecycle.setSession(session);
    return session;
  }
}
