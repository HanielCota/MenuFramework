package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.api.MenuPreloader;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

/**
 * Default implementation of {@link MenuPreloader} that pre-computes menu content asynchronously.
 */
public final class DefaultMenuPreloader implements MenuPreloader {

  private static final Logger log = Logger.getLogger(DefaultMenuPreloader.class.getName());

  @NonNull private final Plugin plugin;
  @NonNull private final SchedulerAdapter scheduler;
  private final Supplier<MenuRuntime> runtimeSupplier;
  private final ConcurrentHashMap<String, PreloadState> preloadStates = new ConcurrentHashMap<>();
  private final Executor asyncExecutor;

  public DefaultMenuPreloader(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      Supplier<MenuRuntime> runtimeSupplier) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.runtimeSupplier = Objects.requireNonNull(runtimeSupplier, "runtimeSupplier");
    this.asyncExecutor = task -> scheduler.runAsync(plugin, task);
  }

  private static @NonNull List<SlotDefinition> sanitizeDynamicContent(List<SlotDefinition> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }
    return items.stream().filter(Objects::nonNull).toList();
  }

  private MenuRuntime runtime() {
    return runtimeSupplier.get();
  }

  @Override
  public CompletableFuture<Void> preload(@NonNull String menuId) {
    Objects.requireNonNull(menuId, "menuId");
    return CompletableFuture.runAsync(() -> doPreload(menuId, null), asyncExecutor);
  }

  @Override
  public CompletableFuture<Void> preload(@NonNull Player player, @NonNull String menuId) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(menuId, "menuId");
    return CompletableFuture.runAsync(() -> doPreload(menuId, player), asyncExecutor);
  }

  @Override
  public CompletableFuture<Void> preloadAll(@NonNull String... menuIds) {
    Objects.requireNonNull(menuIds, "menuIds");
    var futures = Arrays.stream(menuIds).map(this::preload).toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(futures);
  }

  @Override
  public void invalidate(@NonNull String menuId) {
    Objects.requireNonNull(menuId, "menuId");
    preloadStates.remove(menuId);
    runtime()
        .definitions()
        .getDefinition(menuId)
        .ifPresent(def -> runtime().paginationEngine().invalidate(menuId));
  }

  @Override
  public void invalidateAll() {
    preloadStates.clear();
    runtime().paginationEngine().invalidateAll();
  }

  private void doPreload(@NonNull String menuId, Player player) {
    var definitionOpt = runtime().definitions().getDefinition(menuId);
    if (definitionOpt.isEmpty()) {
      log.warning(() -> "Cannot preload menu '%s': definition not found".formatted(menuId));
      return;
    }

    var definition = definitionOpt.get();
    var state = new PreloadState(menuId, System.currentTimeMillis());
    var existing = preloadStates.putIfAbsent(menuId, state);
    if (existing != null && existing.isCompleted()) {
      return;
    }

    try {
      // Resolve dynamic content
      List<SlotDefinition> dynamicItems = resolveDynamicContent(definition, player);

      // Pre-compute pagination if enabled
      if (definition.pagination().enabled() && !dynamicItems.isEmpty()) {
        var pagination = definition.pagination();
        var contentSlots = pagination.contentSlots();
        var itemsPerPage = contentSlots.size();
        var totalPages = Math.max(1, (int) Math.ceil((double) dynamicItems.size() / itemsPerPage));
        var contentHash = runtime().definitions().getDynamicContentHash(menuId);
        var slots = definition.size();

        // Pre-compute first few pages (cap at 5 to avoid excessive work)
        var pagesToPrecompute = Math.min(totalPages, 5);
        for (int page = 0; page < pagesToPrecompute; page++) {
          runtime()
              .paginationEngine()
              .getOrBuildPage(definition, dynamicItems, page, slots, contentHash);
        }
      }

      state.markCompleted();
      log.fine(() -> "Preloaded menu '%s' in %dms".formatted(menuId, state.durationMs()));
    } catch (Exception e) {
      state.markFailed(e);
      log.log(Level.WARNING, e, () -> "Failed to preload menu '%s'".formatted(menuId));
    }
  }

  private List<SlotDefinition> resolveDynamicContent(
      @NonNull MenuDefinition definition, Player player) {
    var menuId = definition.id();
    var providerOpt = runtime().definitions().getDynamicContentProvider(menuId);

    if (providerOpt.isPresent() && player != null) {
      try {
        var session = new MockSession(player.getUniqueId(), definition);
        return sanitizeDynamicContent(providerOpt.get().provide(player, session));
      } catch (Exception e) {
        log.log(
            Level.WARNING, e, () -> "Dynamic content provider failed for '%s'".formatted(menuId));
      }
    }

    return runtime().definitions().getDynamicContent(menuId);
  }

  /** Returns the preload state for a menu, or null if not preloaded. */
  public PreloadState getState(@NonNull String menuId) {
    return preloadStates.get(menuId);
  }

  /** Internal state tracking for preloading operations. */
  public static final class PreloadState {
    private final String menuId;
    private final long startTime;
    private volatile long endTime;
    private volatile boolean completed;
    private volatile Exception failure;

    PreloadState(@NonNull String menuId, long startTime) {
      this.menuId = menuId;
      this.startTime = startTime;
    }

    void markCompleted() {
      this.endTime = System.currentTimeMillis();
      this.completed = true;
    }

    void markFailed(@NonNull Exception e) {
      this.endTime = System.currentTimeMillis();
      this.failure = e;
    }

    public boolean isCompleted() {
      return completed;
    }

    public boolean isFailed() {
      return failure != null;
    }

    public long durationMs() {
      return endTime - startTime;
    }

    public Exception failure() {
      return failure;
    }

    public @NonNull String menuId() {
      return menuId;
    }
  }

  /** Minimal mock session for dynamic content providers during preload. */
  private record MockSession(UUID viewerId, MenuDefinition definition) implements MenuSession {

    @Override
    public String menuId() {
      return definition.id();
    }

    @Override
    public InventoryView view() {
      throw new UnsupportedOperationException("Mock session has no view");
    }

    @Override
    public int currentPage() {
      return 0;
    }

    @Override
    public void setPage(int page) {
      throw new UnsupportedOperationException("Mock session cannot change page");
    }

    @Override
    public void refresh() {
      // No-op for mock
    }

    @Override
    public void updateSlot(int slot, ItemTemplate template) {
      throw new UnsupportedOperationException("Mock session cannot update slots");
    }

    @Override
    public void updateSlots(Map<Integer, ItemTemplate> slots) {
      throw new UnsupportedOperationException("Mock session cannot update slots");
    }

    @Override
    public void close() {
      // No-op for mock
    }

    @Override
    public boolean isSameView(InventoryView other) {
      return false;
    }

    @Override
    public @NonNull CompletableFuture<Void> dispose() {
      return CompletableFuture.completedFuture(null);
    }

    public void disposeImmediately() {
      // No-op for mock
    }
  }
}
