package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuHistory;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;

public final class PlayerMenuHistory implements MenuHistory {

  private static final int MAX_HISTORY_SIZE = 10;

  private final ConcurrentHashMap<UUID, Deque<String>> histories = new ConcurrentHashMap<>();

  @Override
  public void push(@NonNull UUID playerUuid, @NonNull String menuId) {
    histories.compute(
        playerUuid,
        (k, history) -> {
          var deque = history != null ? history : new ArrayDeque<String>();
          synchronized (deque) {
            // Don't push the same menu twice in a row
            if (!deque.isEmpty() && deque.peekLast().equals(menuId)) {
              return deque;
            }
            deque.addLast(menuId);
            // Limit history size
            while (deque.size() > MAX_HISTORY_SIZE) {
              deque.removeFirst();
            }
          }
          return deque;
        });
  }

  @Override
  public @NonNull Optional<String> pop(@NonNull UUID playerUuid) {
    var history = histories.get(playerUuid);
    if (history == null || history.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(history.removeLast());
  }

  @Override
  public @NonNull Optional<String> peek(@NonNull UUID playerUuid) {
    var history = histories.get(playerUuid);
    if (history == null || history.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(history.peekLast());
  }

  @Override
  public boolean hasHistory(@NonNull UUID playerUuid) {
    var history = histories.get(playerUuid);
    return history != null && !history.isEmpty();
  }

  @Override
  public void clear(@NonNull UUID playerUuid) {
    histories.remove(playerUuid);
  }

  @Override
  public @NonNull Deque<String> getHistory(@NonNull UUID playerUuid) {
    var history = histories.get(playerUuid);
    if (history == null) {
      return new ArrayDeque<>();
    }
    return new ArrayDeque<>(history);
  }
}
