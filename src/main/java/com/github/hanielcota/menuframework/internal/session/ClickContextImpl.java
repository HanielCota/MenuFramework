package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuHistory;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.text.MiniMessageProvider;
import com.github.hanielcota.menuframework.messaging.MessageKey;
import com.github.hanielcota.menuframework.messaging.MessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public record ClickContextImpl(
    @NonNull MenuSession session,
    @NonNull Player player,
    int rawSlot,
    @NonNull ClickType clickType,
    @NonNull MenuService menuService,
    @NonNull MenuHistory menuHistory,
    @NonNull MessageService messageService)
    implements ClickContext {

  @Override
  public @NonNull Audience audience() {
    return player;
  }

  @Override
  public int slot() {
    return rawSlot;
  }

  @Override
  public void reply(@NonNull Component message) {
    audience().sendMessage(message);
  }

  @Override
  public void reply(@NonNull String miniMessage) {
    audience().sendMessage(MiniMessageProvider.deserialize(miniMessage));
  }

  @Override
  public void close() {
    session.close();
  }

  @Override
  public void open(@NonNull String menuId) {
    menuHistory.push(player.getUniqueId(), session.menuId());
    var future = menuService.open(player.getUniqueId(), menuId);
    if (future == null) return;
    future.exceptionally(
        openFailure -> {
          messageService.send(player, MessageKey.MENU_OPEN_ERROR, openFailure.getMessage());
          return null;
        });
  }

  @Override
  public void refresh() {
    session.refresh();
  }

  @Override
  public void setPage(int page) {
    session.setPage(page);
  }

  @Override
  public int currentPage() {
    return session.currentPage();
  }

  @Override
  public @NonNull Plugin plugin() {
    return menuService.getPlugin();
  }

  @Override
  public void back() {
    var previous = menuHistory.pop(player.getUniqueId());
    previous.ifPresent(
        menuId -> {
          var future = menuService.open(player.getUniqueId(), menuId);
          if (future != null) {
            future.exceptionally(
                openFailure -> {
                  messageService.send(player, MessageKey.MENU_OPEN_ERROR, openFailure.getMessage());
                  return null;
                });
          }
        });
  }

  @Override
  public boolean hasPreviousMenu() {
    return menuHistory.hasHistory(player.getUniqueId());
  }
}
