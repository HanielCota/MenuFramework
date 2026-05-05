package com.github.hanielcota.menuframework.internal.registry;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.cache.MenuCacheFactory;
import com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession;
import com.github.hanielcota.menuframework.internal.session.MenuSessionImpl;
import com.github.hanielcota.menuframework.internal.session.SessionCommands;
import com.github.hanielcota.menuframework.internal.session.SessionQuery;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SessionRegistry implements SessionQuery, SessionCommands {

  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(SessionRegistry.class.getName());

  @NonNull private final Cache<UUID, MenuSessionImpl> sessionCache;

  public SessionRegistry(@NonNull MenuFrameworkConfig configuration) {
    this.sessionCache = MenuCacheFactory.createSessionCache(configuration, this::onSessionRemoved);
  }

  private void onSessionRemoved(
      @Nullable UUID playerUuid,
      @Nullable MenuSessionImpl removedSession,
      RemovalCause removalCause) {
    if (removedSession == null) return;
    log.log(java.util.logging.Level.FINE, "Session removed: %s cause=%s".formatted(playerUuid, removalCause));
    try {
      removedSession.disposeImmediately();
    } catch (Exception exception) {
      log.log(java.util.logging.Level.WARNING, "Error disposing session on removal", exception);
    }
  }

  @Override
  public void register(@NonNull UUID playerUuid, @NonNull MenuSessionImpl session) {
    sessionCache.put(playerUuid, session);
  }

  @Override
  public @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid) {
    return Optional.ofNullable(sessionCache.getIfPresent(playerUuid));
  }

  @Override
  public @NonNull Optional<@NonNull InteractiveMenuSession> getInteractiveSession(@NonNull UUID playerUuid) {
    return Optional.ofNullable(sessionCache.getIfPresent(playerUuid));
  }

  @Override
  public void closeSession(@NonNull UUID playerUuid) {
    sessionCache.invalidate(playerUuid);
  }

  @Override
  public void closeAllSessions() {
    sessionCache.invalidateAll();
  }

  public long estimatedSessionCount() {
    return sessionCache.estimatedSize();
  }

  public double sessionHitRate() {
    return sessionCache.stats().hitRate();
  }
}
