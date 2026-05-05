package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.internal.render.RenderEngine;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SessionFactory {

  private static final Logger log = Logger.getLogger(SessionFactory.class.getName());

  @NonNull
  private final Plugin plugin;
  @NonNull
  private final SchedulerAdapter scheduler;
  @NonNull
  private final ServerAccess serverAccess;
  @NonNull
  private final RenderEngine renderEngine;
  @NonNull
  private final MenuService menuService;
  @NonNull
  private final SessionCommands sessionCommands;
  @NonNull
  private final RefreshScheduler refreshScheduler;
  @NonNull
  private final MenuSessionImplFactory sessionImplFactory;

  public SessionFactory(
      @NonNull Plugin plugin,
      @NonNull SchedulerAdapter scheduler,
      @NonNull ServerAccess serverAccess,
      @NonNull RenderEngine renderEngine,
      @NonNull MenuService menuService,
      @NonNull SessionCommands sessionCommands,
      @NonNull RefreshScheduler refreshScheduler,
      @NonNull MenuSessionImplFactory sessionImplFactory) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.serverAccess = serverAccess;
    this.renderEngine = renderEngine;
    this.menuService = menuService;
    this.sessionCommands = sessionCommands;
    this.refreshScheduler = refreshScheduler;
    this.sessionImplFactory = sessionImplFactory;
  }

  private static void fireOpenFeatures(@NonNull MenuSessionImpl session, @NonNull MenuDefinition definition) {
    for (MenuFeature feature : definition.features()) {
      try {
        feature.onOpen(session);
      } catch (Exception exception) {
        log.log(
            Level.WARNING,
            exception,
            () ->
                "menu.feature.onOpen_failed menuId=%s featureType=%s"
                    .formatted(definition.id(), feature.getClass().getSimpleName()));
      }
    }
  }

  public @NonNull CompletableFuture<MenuSession> create(
      @NonNull UUID playerUuid, @NonNull MenuDefinition definition) {
    CompletableFuture<MenuSession> future = new CompletableFuture<>();
    scheduler.runSync(plugin, () -> completeSessionCreation(future, playerUuid, definition));
    return future;
  }

  private void completeSessionCreation(
      @NonNull CompletableFuture<MenuSession> future, @NonNull UUID playerUuid, @NonNull MenuDefinition definition) {
    try {
      var player = serverAccess.findOnlinePlayer(playerUuid).orElse(null);
      if (player == null) {
        future.completeExceptionally(new IllegalStateException("Player offline"));
        return;
      }

      var session = openSession(player, playerUuid, definition);
      if (session == null) {
        future.completeExceptionally(new IllegalStateException("Could not open inventory"));
        return;
      }

      try {
        fireOpenFeatures(session, definition);
        scheduleRefresh(session);
        sessionCommands.register(playerUuid, session);
        future.complete(session);
      } catch (Exception exception) {
        session.close();
        future.completeExceptionally(exception);
      }
    } catch (Exception exception) {
      future.completeExceptionally(exception);
    }
  }

  private @Nullable MenuSessionImpl openSession(@NonNull Player player, @NonNull UUID playerUuid, @NonNull MenuDefinition definition) {
    var inventory = serverAccess.createInventory(player, definition);
    var view = player.openInventory(inventory);

    if (view == null || !view.getTopInventory().equals(inventory)) {
      return null;
    }

    var session = sessionImplFactory.create(playerUuid, definition, view);
    session.refresh();
    return session;
  }

  private void scheduleRefresh(@NonNull MenuSessionImpl session) {
    var task = refreshScheduler.schedule(session);
    if (task != null) {
      session.setRefreshTaskHandle(task);
    }
  }
}
