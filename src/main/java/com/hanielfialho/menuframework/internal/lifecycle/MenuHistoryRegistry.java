package com.hanielfialho.menuframework.internal.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Histórico limitado e concorrente por jogador.
 *
 * <p>As listas armazenadas no mapa são snapshots imutáveis. Toda mutação utiliza
 * ConcurrentMap#compute, evitando exposição de Deque mutável para outras threads durante quit ou
 * shutdown.
 */
public final class MenuHistoryRegistry {

  public static final int DEFAULT_MAX_DEPTH = 32;

  private final int maxDepth;
  private final ConcurrentMap<UUID, List<MenuHistoryEntry<?>>> histories;

  public MenuHistoryRegistry() {
    this(DEFAULT_MAX_DEPTH);
  }

  public MenuHistoryRegistry(int maxDepth) {
    if (maxDepth <= 0) {
      throw new IllegalArgumentException("maxDepth must be greater than zero: " + maxDepth);
    }

    this.maxDepth = maxDepth;
    this.histories = new ConcurrentHashMap<>();
  }

  public int maxDepth() {
    return this.maxDepth;
  }

  public int depth(UUID viewerId) {
    List<MenuHistoryEntry<?>> history =
        this.histories.get(Objects.requireNonNull(viewerId, "viewerId"));

    return history == null ? 0 : history.size();
  }

  public MenuHistoryEntry<?> peek(UUID viewerId) {
    List<MenuHistoryEntry<?>> history =
        this.histories.get(Objects.requireNonNull(viewerId, "viewerId"));

    if (history == null || history.isEmpty()) {
      return null;
    }

    return history.getLast();
  }

  public boolean topIs(UUID viewerId, MenuHistoryEntry<?> expected) {
    Objects.requireNonNull(expected, "expected");
    return this.peek(viewerId) == expected;
  }

  public int push(UUID viewerId, MenuHistoryEntry<?> entry) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(entry, "entry");

    AtomicInteger resultingDepth = new AtomicInteger();

    this.histories.compute(
        viewerId,
        (ignored, current) -> {
          List<MenuHistoryEntry<?>> previous = current == null ? List.of() : current;

          int retained = Math.min(previous.size(), this.maxDepth - 1);

          int firstRetainedIndex = previous.size() - retained;

          List<MenuHistoryEntry<?>> next = new ArrayList<>(retained + 1);

          if (retained > 0) {
            next.addAll(previous.subList(firstRetainedIndex, previous.size()));
          }

          next.add(entry);
          resultingDepth.set(next.size());
          return List.copyOf(next);
        });

    return resultingDepth.get();
  }

  public boolean popIfTop(UUID viewerId, MenuHistoryEntry<?> expected) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(expected, "expected");

    AtomicBoolean popped = new AtomicBoolean();

    this.histories.computeIfPresent(
        viewerId,
        (ignored, current) -> {
          int lastIndex = current.size() - 1;

          if (lastIndex < 0 || current.get(lastIndex) != expected) {
            return current;
          }

          popped.set(true);

          if (lastIndex == 0) {
            return null;
          }

          return List.copyOf(current.subList(0, lastIndex));
        });

    return popped.get();
  }

  public void clear(UUID viewerId) {
    this.histories.remove(Objects.requireNonNull(viewerId, "viewerId"));
  }

  public void clearAll() {
    this.histories.clear();
  }
}
