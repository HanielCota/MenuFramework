package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.internal.interaction.MenuInteractionController;
import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class MenuSessionImpl implements MenuSession, InteractiveMenuSession {

  @NonNull private final MenuSessionState state;
  private final SessionRenderer renderer;
  @NonNull private final MenuInteractionController interactions;
  private final SessionLifecycle lifecycle;

  public MenuSessionImpl(
      @NonNull MenuSessionState state,
      @NonNull SessionRenderer renderer,
      @NonNull MenuInteractionController interactions,
      @NonNull SessionLifecycle lifecycle) {
    this.state = state;
    this.renderer = renderer;
    this.interactions = interactions;
    this.lifecycle = lifecycle;
  }

  @Override
  public @NonNull UUID viewerId() {
    return state.viewerId();
  }

  @Override
  public @NonNull String menuId() {
    return state.definition().id();
  }

  @Override
  public @NonNull InventoryView view() {
    return state.view();
  }

  @Override
  public int currentPage() {
    return state.currentPage();
  }

  @Override
  public void setPage(int page) {
    if (state.disposed()) {
      throw new IllegalStateException("Session is disposed");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page cannot be negative: " + page);
    }
    if (page == state.currentPage()) return;
    state.currentPage(page);
    refresh();
  }

  @Override
  public boolean handleClick(int rawSlot, @NonNull ClickType clickType) {
    return interactions.handleClick(rawSlot, clickType);
  }

  @Override
  public void refresh() {
    renderer.refresh();
  }

  public void setRefreshTaskHandle(@NonNull Object handle) {
    lifecycle.setRefreshTaskHandle(handle);
  }

  @Override
  public void close() {
    lifecycle.disposeImmediately();
  }

  @Override
  public boolean isSameView(@NonNull InventoryView other) {
    return state.view().getTopInventory().equals(other.getTopInventory());
  }

  @Override
  public @NonNull CompletableFuture<Void> dispose() {
    return lifecycle.dispose();
  }

  @Override
  public void disposeImmediately() {
    lifecycle.disposeImmediately();
  }

  List<com.github.hanielcota.menuframework.api.MenuFeature> features() {
    return state.definition().features();
  }

  boolean disposed() {
    return state.disposed();
  }
}
