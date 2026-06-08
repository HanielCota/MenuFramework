package dev.haniel.menu.example.service;

import dev.haniel.menu.paper.MenuFramework;
import dev.haniel.menu.paper.registry.ReloadFailure;
import dev.haniel.menu.paper.registry.ReloadReport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Reloads framework menus and reports the outcome, keeping the permission check in one place. */
public final class MenuReloader {

  private final MenuMessages messages;
  private final Logger logger;
  private final Plugin plugin;
  private MenuFramework framework;

  public MenuReloader(MenuMessages messages, Logger logger) {
    this(messages, logger, null);
  }

  public MenuReloader(MenuMessages messages, Logger logger, Plugin plugin) {
    this.messages = messages;
    this.logger = logger;
    this.plugin = plugin;
  }

  public void attach(MenuFramework framework) {
    this.framework = framework;
  }

  public void reloadAll(Player player) {
    if (!player.hasPermission("menuexample.reload")) {
      messages.send(player, "<red>You do not have permission to reload menus.</red>");
      return;
    }
    if (framework == null) {
      messages.send(player, "<red>Menu framework is not ready.</red>");
      return;
    }
    framework
        .reloadAllReportAsync()
        .thenAccept(report -> scheduleReport(player, report))
        .exceptionally(error -> logFailure(error));
  }

  private void scheduleReport(Player player, ReloadReport report) {
    if (plugin == null) {
      if (player.isOnline()) {
        report(player, report);
      }
      return;
    }
    player.getScheduler().run(plugin, ignored -> reportIfOnline(player, report), () -> {});
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
