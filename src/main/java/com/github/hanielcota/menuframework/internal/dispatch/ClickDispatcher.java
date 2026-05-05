package com.github.hanielcota.menuframework.internal.dispatch;

import com.github.hanielcota.menuframework.internal.session.InteractiveMenuSession;
import com.github.hanielcota.menuframework.internal.session.SessionQuery;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class ClickDispatcher {

  @NonNull private final SessionQuery sessionQuery;

  public boolean dispatchClick(
      @NonNull Player player,
      @NonNull InventoryView view,
      int rawSlot,
      @NonNull ClickType clickType) {
    return findSession(player, view)
        .map(session -> session.handleClick(rawSlot, clickType))
        .orElse(false);
  }

  public boolean dispatchDrag(@NonNull Player player, @NonNull InventoryView view) {
    return findSession(player, view).isPresent();
  }

  public boolean dispatchClose(@NonNull Player player, @NonNull InventoryView view) {
    return findSession(player, view).isPresent();
  }

  private @NonNull Optional<@NonNull InteractiveMenuSession> findSession(
      @NonNull Player player, @NonNull InventoryView view) {
    return sessionQuery
        .getInteractiveSession(player.getUniqueId())
        .filter(session -> session.isSameView(view));
  }
}
