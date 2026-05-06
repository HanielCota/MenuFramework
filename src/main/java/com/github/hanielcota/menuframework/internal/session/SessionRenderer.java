package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class SessionRenderer {
  private static final Logger log = Logger.getLogger(SessionRenderer.class.getName());

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final RenderEngine renderEngine;
  @NonNull private final MenuSessionState state;
  @NonNull private final ActiveSlotRegistry activeSlots;
  @NonNull private final PlayerResolver playerResolver;
  @NonNull private final ServerAccess serverAccess;
  @NonNull private final ItemStackFactory itemStackFactory;

  public SessionRenderer(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull RenderEngine renderEngine,
      @NonNull MenuSessionState state,
      @NonNull ActiveSlotRegistry activeSlots,
      @NonNull PlayerResolver playerResolver,
      @NonNull ServerAccess serverAccess,
      @NonNull ItemStackFactory itemStackFactory) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.renderEngine = renderEngine;
    this.state = state;
    this.activeSlots = activeSlots;
    this.playerResolver = playerResolver;
    this.serverAccess = serverAccess;
    this.itemStackFactory = itemStackFactory;
  }

  public void refresh() {
    if (state.disposed()) return;
    if (serverAccess.isNotPrimaryThread()) {
      scheduler.runSync(plugin, this::refresh);
      return;
    }
    if (playerResolver.resolveOnline(state.viewerId()) == null) return;

    try {
      var result = renderEngine.render(state.view(), state.definition(), state.currentPage());
      if (state.disposed()) return;
      state.currentPage(result.resolvedPage());
      activeSlots.replaceWith(result);
      reapplyToggleStates();
    } catch (Exception exception) {
      log.log(Level.SEVERE, "Render error in menu " + state.definition().id(), exception);
    }
  }

  private void reapplyToggleStates() {
    for (var entry : state.toggleStates().entrySet()) {
      var slot = entry.getKey();
      var toggleState = entry.getValue();
      var view = state.view();
      var topInventory = view.getTopInventory();
      if (slot >= 0 && slot < topInventory.getSize()) {
        topInventory.setItem(slot, itemStackFactory.create(toggleState.currentTemplate()));
      }
    }
  }

  public void updateSlot(int slot, @NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    if (state.disposed()) return;
    if (serverAccess.isNotPrimaryThread()) {
      scheduler.runSync(plugin, () -> updateSlot(slot, template));
      return;
    }
    if (playerResolver.resolveOnline(state.viewerId()) == null) return;

    try {
      var view = state.view();
      var topInventory = view.getTopInventory();
      if (slot >= 0 && slot < topInventory.getSize()) {
        topInventory.setItem(slot, itemStackFactory.create(template));
      }
    } catch (Exception exception) {
      log.log(
          Level.SEVERE,
          "Error updating slot " + slot + " in menu " + state.definition().id(),
          exception);
    }
  }
}
