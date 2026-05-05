package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.internal.text.MiniMessageProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public record ClickContextImpl(@NonNull MenuSession session, @NonNull Player player, int rawSlot, @NonNull ClickType clickType,
                               @NonNull MenuService menuService) implements ClickContext {

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
    player.sendMessage(message);
  }

  @Override
  public void reply(@NonNull String miniMessage) {
    player.sendMessage(MiniMessageProvider.deserialize(miniMessage));
  }

  @Override
  public void close() {
    session.close();
  }

  @Override
  public void open(@NonNull String menuId) {
    var future = menuService.open(player.getUniqueId(), menuId);
    if (future == null) return;
    future.exceptionally(
        openFailure -> {
          player.sendMessage(Component.text("Erro ao abrir menu: " + openFailure.getMessage()));
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
}
