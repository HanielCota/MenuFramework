package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuSession;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public interface SessionQuery {

  @NonNull Optional<@NonNull MenuSession> getSession(@NonNull UUID playerUuid);

  @NonNull Optional<@NonNull InteractiveMenuSession> getInteractiveSession(
      @NonNull UUID playerUuid);
}
