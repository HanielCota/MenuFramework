package dev.haniel.menu.example.service;

import dev.haniel.menu.paper.MenuFramework;
import dev.haniel.menu.paper.registry.ReloadFailure;
import dev.haniel.menu.paper.registry.ReloadReport;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Reloads framework menus and reports the outcome, keeping the permission check in one place. */
public final class MenuReloader {

  private final MenuMessages messages;
  private final Logger logger;
  private final Plugin plugin;
  private final Supplier<MenuFramework> framework;

  public MenuReloader(MenuMessages messages, Logger logger, Supplier<MenuFramework> framework) {
    this(messages, logger, null, framework);
  }

  public MenuReloader(
      MenuMessages messages, Logger logger, Plugin plugin, Supplier<MenuFramework> framework) {
    this.messages = messages;
    this.logger = logger;
    this.plugin = plugin;
    this.framework = framework;
  }

  public void reloadAll(Player player) {
    if (!player.hasPermission("menuexample.reload")) {
      messages.send(player, "<red>You do not have permission to reload menus.</red>");
      return;
    }
    MenuFramework menus = framework.get();
    if (menus == null) {
      messages.send(player, "<red>Menu framework is not ready.</red>");
      return;
    }
    if (plugin == null) {
      menus
          .reloadAllReportAsync()
          .thenAccept(report -> reportIfOnline(player, report))
          .exceptionally(error -> logFailure(error));
      return;
    }
    UUID viewer = player.getUniqueId();
    EntityScheduler scheduler = player.getScheduler();
    menus
        .reloadAllReportAsync()
        .thenAccept(report -> scheduleReport(scheduler, viewer, report))
        .exceptionally(error -> logFailure(error));
  }

  private void scheduleReport(EntityScheduler scheduler, UUID viewer, ReloadReport report) {
    try {
      scheduler.run(plugin, ignored -> reportIfOnline(viewer, report), () -> {});
    } catch (RuntimeException unschedulable) {
      // The player left or is no longer schedulable; the reload itself succeeded, so a lost report
      // must not surface as a reload failure through the future's exceptionally handler.
      logger.log(Level.FINE, "Skipped reload report for an unschedulable player", unschedulable);
    }
  }

  private void reportIfOnline(UUID viewer, ReloadReport report) {
    Player player = Bukkit.getPlayer(viewer);
    if (player != null && player.isOnline()) {
      report(player, report);
    }
  }

  private void reportIfOnline(Player player, ReloadReport report) {
    if (player.isOnline()) {
      report(player, report);
    }
  }

  private Void logFailure(Throwable error) {
    logger.log(Level.SEVERE, "Asynchronous menu reload failed", error);
    return null;
  }

  private void report(Player player, ReloadReport report) {
    if (report.successful()) {
      messages.send(player, "<green>Reloaded " + report.successCount() + " menu(s).</green>");
      return;
    }
    messages.send(player, "<red>Reloaded with " + report.failures().size() + " failure(s).</red>");
    report.failures().stream()
        .map(this::failureMessage)
        .forEach(message -> messages.send(player, message));
  }

  private String failureMessage(ReloadFailure failure) {
    return "<gray>- " + failure.id().value() + ": " + failure.message() + "</gray>";
  }
}
