package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuHistory;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.internal.interaction.ClickExecutor;
import com.github.hanielcota.menuframework.internal.interaction.InteractionPolicy;
import com.github.hanielcota.menuframework.internal.interaction.MenuInteractionController;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.messaging.MessageService;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.UUID;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class MenuSessionImplFactory {

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final ServerAccess serverAccess;
  @NonNull private final RenderEngine renderEngine;
  @NonNull private final MenuService menuService;
  @NonNull private final ClickExecutor clickExecutor;
  @NonNull private final ItemStackFactory itemStackFactory;
  @NonNull private final MenuHistory menuHistory;
  @NonNull private final MessageService messageService;

  public MenuSessionImplFactory(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull ServerAccess serverAccess,
      @NonNull RenderEngine renderEngine,
      @NonNull MenuService menuService,
      @NonNull ClickExecutor clickExecutor,
      @NonNull ItemStackFactory itemStackFactory,
      @NonNull MenuHistory menuHistory,
      @NonNull MessageService messageService) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.serverAccess = serverAccess;
    this.renderEngine = renderEngine;
    this.menuService = menuService;
    this.clickExecutor = clickExecutor;
    this.itemStackFactory = itemStackFactory;
    this.menuHistory = menuHistory;
    this.messageService = messageService;
  }

  public @NonNull MenuSessionImpl create(
      @NonNull UUID viewerId, @NonNull MenuDefinition definition, @NonNull InventoryView view) {
    var state = new MenuSessionState(viewerId, definition, view);
    var activeSlots = new ActiveSlotRegistry();
    var playerResolver = new PlayerResolver(serverAccess);

    var renderer =
        new SessionRenderer(
            plugin, scheduler, renderEngine, state, activeSlots, playerResolver, serverAccess, itemStackFactory);
    var interactions =
        new MenuInteractionController(
            state, activeSlots, playerResolver, clickExecutor, new InteractionPolicy(), menuService, menuHistory, messageService);

    var session = new MenuSessionImpl(state, renderer, interactions, null);
    var lifecycle = new SessionLifecycle(plugin, scheduler, session, state, activeSlots, serverAccess);
    session.setLifecycle(lifecycle);
    return session;
  }
}
