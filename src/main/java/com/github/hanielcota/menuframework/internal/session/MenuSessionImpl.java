package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.internal.interaction.MenuInteractionController;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class MenuSessionImpl implements MenuSession, InteractiveMenuSession {

  @NonNull private final MenuSessionState state;
  private final SessionRenderer renderer;
  @NonNull private final MenuInteractionController interactions;
  private SessionLifecycle lifecycle;

  public MenuSessionImpl(
      @NonNull MenuSessionState state,
      @NonNull SessionRenderer renderer,
      @NonNull MenuInteractionController interactions,
      SessionLifecycle lifecycle) {
    this.state = state;
    this.renderer = renderer;
    this.interactions = interactions;
    this.lifecycle = lifecycle;
  }

  void setLifecycle(@NonNull SessionLifecycle lifecycle) {
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
  public synchronized void setPage(int page) {
    if (state.disposed()) {
      throw new IllegalStateException("Session is disposed");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page cannot be negative: " + page);
    }
    if (page == state.currentPage()) return;
    int previousPage = state.currentPage();
    state.currentPage(page);
    try {
      refresh();
    } catch (Exception e) {
      state.currentPage(previousPage);
      throw e;
    }
  }

  @Override
  public boolean handleClick(int rawSlot, @NonNull ClickType clickType) {
    return interactions.handleClick(rawSlot, clickType);
  }

  @Override
  public void refresh() {
    renderer.refresh();
  }

  @Override
  public void updateSlot(int slot, @NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    if (state.disposed()) {
      throw new IllegalStateException("Session is disposed");
    }
    renderer.updateSlot(slot, template);
  }

  @Override
  public void updateSlots(@NonNull Map<Integer, ItemTemplate> slots) {
    Objects.requireNonNull(slots, "slots");
    if (state.disposed()) {
      throw new IllegalStateException("Session is disposed");
    }
    for (var entry : slots.entrySet()) {
      var slot = Objects.requireNonNull(entry.getKey(), "slot");
      var template = Objects.requireNonNull(entry.getValue(), "template");
      renderer.updateSlot(slot, template);
    }
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
    // Use reference equality for inventories to avoid false positives
    return state.view().getTopInventory() == other.getTopInventory();
  }

  @Override
  public @NonNull CompletableFuture<Void> dispose() {
    if (lifecycle == null) {
      return CompletableFuture.completedFuture(null);
    }
    return lifecycle.dispose();
  }

  @Override
  public void disposeImmediately() {
    if (lifecycle != null) {
      lifecycle.disposeImmediately();
    }
  }

  List<MenuFeature> features() {
    return state.definition().features();
  }

  boolean disposed() {
    return state.disposed();
  }

  public @NonNull MenuSessionState state() {
    return state;
  }
}
