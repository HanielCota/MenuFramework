package com.hanielfialho.menuframework.internal.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Concurrent registry of the current session per viewer and every live session.
 *
 * <p>The live-session set is deliberately separate from the current-session map. During an
 * inventory replacement, the previous session is temporarily detached from the map but still needs
 * to be cancelled if shutdown races with the replacement.
 */
public final class MenuSessionRegistry {

  private final ConcurrentMap<UUID, MenuSession<?>> currentSessions = new ConcurrentHashMap<>();

  private final Set<MenuSession<?>> liveSessions = ConcurrentHashMap.newKeySet();

  public MenuSession<?> get(UUID viewerId) {
    return this.currentSessions.get(Objects.requireNonNull(viewerId, "viewerId"));
  }

  public MenuSession<?> publish(UUID viewerId, MenuSession<?> session) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(session, "session");

    if (!viewerId.equals(session.viewerId())) {
      throw new IllegalArgumentException("Viewer id does not match the menu session owner");
    }

    this.liveSessions.add(session);
    return this.currentSessions.put(viewerId, session);
  }

  public MenuSession<?> remove(UUID viewerId) {
    return this.currentSessions.remove(Objects.requireNonNull(viewerId, "viewerId"));
  }

  public boolean remove(UUID viewerId, MenuSession<?> expected) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(expected, "expected");

    return this.currentSessions.remove(viewerId, expected);
  }

  public boolean restore(UUID viewerId, MenuSession<?> expected, MenuSession<?> replacement) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(expected, "expected");
    Objects.requireNonNull(replacement, "replacement");

    if (!viewerId.equals(replacement.viewerId())) {
      throw new IllegalArgumentException("Viewer id does not match the replacement session owner");
    }

    return this.currentSessions.replace(viewerId, expected, replacement);
  }

  public void untrack(MenuSession<?> session) {
    this.liveSessions.remove(Objects.requireNonNull(session, "session"));
  }

  public MenuSession<?> current(UUID viewerId, UUID sessionId) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(sessionId, "sessionId");

    MenuSession<?> session = this.currentSessions.get(viewerId);

    if (session == null
        || !session.opened()
        || session.disposed()
        || !session.id().equals(sessionId)) {
      return null;
    }

    return session;
  }

  public boolean isCurrent(MenuSession<?> session) {
    Objects.requireNonNull(session, "session");

    return session.opened()
        && !session.disposed()
        && this.currentSessions.get(session.viewerId()) == session;
  }

  public List<MenuSession<?>> clearAndSnapshotLiveSessions() {
    this.currentSessions.clear();

    List<MenuSession<?>> snapshot = new ArrayList<>(this.liveSessions);
    snapshot.forEach(this.liveSessions::remove);
    return List.copyOf(snapshot);
  }
}
