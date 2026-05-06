package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.ToggleState;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

public final class MenuSessionState {

  @NonNull private final UUID viewerId;
  @NonNull private final MenuDefinition definition;
  @NonNull private final InventoryView view;
  private final AtomicBoolean disposed = new AtomicBoolean(false);
  private final Map<Integer, ToggleState> toggleStates = new ConcurrentHashMap<>();
  private volatile int currentPage;

  public MenuSessionState(
      @NonNull UUID viewerId, @NonNull MenuDefinition definition, @NonNull InventoryView view) {
    this.viewerId = viewerId;
    this.definition = definition;
    this.view = view;
  }

  public @NonNull UUID viewerId() {
    return viewerId;
  }

  public @NonNull MenuDefinition definition() {
    return definition;
  }

  public @NonNull InventoryView view() {
    return view;
  }

  public int currentPage() {
    return currentPage;
  }

  public void currentPage(int currentPage) {
    if (currentPage < 0) {
      throw new IllegalArgumentException("currentPage cannot be negative: " + currentPage);
    }
    this.currentPage = currentPage;
  }

  public boolean disposed() {
    return disposed.get();
  }

  public boolean markDisposed() {
    return disposed.compareAndSet(false, true);
  }

  public @NonNull Map<Integer, ToggleState> toggleStates() {
    return toggleStates;
  }
}
