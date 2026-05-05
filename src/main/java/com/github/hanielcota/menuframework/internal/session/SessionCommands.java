package com.github.hanielcota.menuframework.internal.session;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

public interface SessionCommands {

  void register(@NonNull UUID playerUuid, @NonNull MenuSessionImpl session);

  void closeSession(@NonNull UUID playerUuid);

  void closeAllSessions();
}
