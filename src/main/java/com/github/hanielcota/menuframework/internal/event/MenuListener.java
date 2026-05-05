package com.github.hanielcota.menuframework.internal.event;

import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.internal.dispatch.MenuEventRouter;
import com.github.hanielcota.menuframework.scheduler.SchedulerAdapter;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class MenuListener implements Listener {

  @NonNull private final Plugin plugin;
  @NonNull private final MenuEventRouter router;
  @NonNull private final SchedulerAdapter scheduler;
  @NonNull private final MenuService menuService;

  public MenuListener(
      @NonNull Plugin plugin,
      @NonNull MenuEventRouter router,
      @NonNull SchedulerAdapter scheduler,
      @NonNull MenuService menuService) {
    this.plugin = plugin;
    this.router = router;
    this.scheduler = scheduler;
    this.menuService = menuService;
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
  public void onClick(@NonNull InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (event.getView() == null || event.getClick() == null) return;

    if (router.dispatchClick(player, event.getView(), event.getRawSlot(), event.getClick())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
  public void onDrag(@NonNull InventoryDragEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (event.getView() == null) return;

    if (router.dispatchDrag(player, event.getView())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  public void onClose(@NonNull InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) return;
    if (event.getView() == null) return;

    UUID playerUuid = player.getUniqueId();
    if (!router.dispatchClose(player, event.getView())) return;

    scheduler.runSyncDelayed(
        plugin, () -> closeSessionIfViewStillMatches(playerUuid, event.getView()), 1);
  }

  private void closeSessionIfViewStillMatches(@NonNull UUID playerUuid, @NonNull InventoryView view) {
    router
        .getSession(playerUuid)
        .filter(session -> session.isSameView(view))
        .ifPresent(session -> router.closeSession(playerUuid));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  public void onQuit(@NonNull PlayerQuitEvent event) {
    router.closeSession(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  public void onPluginDisable(@NonNull PluginDisableEvent event) {
    if (event.getPlugin().equals(plugin)) {
      menuService.shutdown();
    }
  }
}
