package com.github.hanielcota.menuframework.internal;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.api.MenuMetrics;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.dispatch.MenuEventRouter;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class DefaultMenuService implements MenuService {

  @NonNull private final Plugin plugin;
  private final SchedulerAdapter scheduler;
  @NonNull private final MenuRuntime runtime;

  public DefaultMenuService(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull MenuFrameworkConfig config) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.runtime = MenuRuntime.create(this, config);
  }

  @Override
  public @NonNull Plugin getPlugin() {
    return plugin;
  }

  public @NonNull SchedulerAdapter getScheduler() {
    return scheduler;
  }

  @Override
  public void registerDefinition(@NonNull MenuDefinition definition) {
    runtime.definitions().registerDefinition(definition);
  }

  @Override
  public @NonNull Optional<@NonNull MenuDefinition> getDefinition(@NonNull String id) {
    return runtime.definitions().getDefinition(id);
  }

  @Override
  public void registerTemplate(@NonNull String id, @NonNull ItemTemplate template) {
    runtime.templates().registerTemplate(id, template);
  }

  @Override
  public @NonNull Optional<@NonNull ItemTemplate> getTemplate(@NonNull String id) {
    return runtime.templates().getTemplate(id);
  }

  @Override
  public void setDynamicContent(@NonNull String menuId, @NonNull List<SlotDefinition> items) {
    runtime.dynamicContent().setDynamicContent(menuId, items);
  }

  @Override
  public void setDynamicContentProvider(
      @NonNull String menuId, @NonNull DynamicContentProvider provider) {
    runtime.dynamicContent().setDynamicContentProvider(menuId, provider);
  }

  @Override
  public @NonNull List<SlotDefinition> getDynamicContent(@NonNull String menuId) {
    return runtime.dynamicContent().getDynamicContent(menuId);
  }

  @Override
  public @NonNull CompletableFuture<MenuSession> open(
      @NonNull Player player, @NonNull String menuId) {
    Objects.requireNonNull(player, "player");
    if (!player.isOnline()) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Player is not online: " + player.getUniqueId()));
    }
    return open(player.getUniqueId(), menuId);
  }

  @Override
  public @NonNull CompletableFuture<MenuSession> open(
      @NonNull UUID playerUuid, @NonNull String menuId) {
    return runtime
        .definitions()
        .getDefinition(menuId)
        .map(definition -> open(playerUuid, definition))
        .orElseGet(
            () ->
                CompletableFuture.failedFuture(
                    new IllegalArgumentException("Menu not found: " + menuId)));
  }

  private @NonNull CompletableFuture<MenuSession> open(
      @NonNull UUID playerUuid, @NonNull MenuDefinition definition) {
    closeSession(playerUuid);
    return runtime.sessionFactory().create(playerUuid, definition);
  }

  @Override
  public @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid) {
    return runtime.sessions().getSession(playerUuid);
  }

  @Override
  public void closeSession(@NonNull UUID playerUuid) {
    runtime.sessionCommands().closeSession(playerUuid);
  }

  @Override
  public void closeAllSessions() {
    runtime.sessionCommands().closeAllSessions();
  }

  @Override
  public void unregisterDefinition(@NonNull String menuId) {
    runtime.definitions().unregisterDefinition(menuId);
  }

  @Override
  public @NonNull MenuMetrics getMetrics() {
    return runtime.metrics();
  }

  @Override
  public void shutdown() {
    runtime.shutdown();
  }

  public @NonNull MenuEventRouter eventRouter() {
    return runtime.eventRouter();
  }
}
