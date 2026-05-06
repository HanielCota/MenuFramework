package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.core.server.ServerAccess;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Resolves an online player by UUID. */
public final class PlayerResolver {

  @NonNull private final ServerAccess serverAccess;

  public PlayerResolver(@NonNull ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  /** Returns the online player for the given UUID, or null if offline. */
  public @Nullable Player resolveOnline(@NonNull UUID viewerId) {
    return serverAccess.findOnlinePlayer(viewerId).orElse(null);
  }
}
