package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.internal.server.ServerAccess;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public final class PlayerResolver {

  @NonNull private final MenuService menuService;
  @NonNull private final ServerAccess serverAccess;

  public @Nullable Player resolveOnline(@NonNull UUID viewerId) {
    return serverAccess
        .findOnlinePlayer(viewerId)
        .orElseGet(
            () -> {
              menuService.closeSession(viewerId);
              return null;
            });
  }
}
