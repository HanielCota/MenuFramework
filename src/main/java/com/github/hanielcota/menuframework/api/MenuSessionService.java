package com.github.hanielcota.menuframework.api;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public interface MenuSessionService {

  /** Returns the active menu session for a player, if one exists. */
  @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid);

  /** Closes and disposes the active session for a player, if one exists. */
  void closeSession(@NonNull UUID playerUuid);

  /** Closes and disposes every active session owned by this service. */
  void closeAllSessions();
}
