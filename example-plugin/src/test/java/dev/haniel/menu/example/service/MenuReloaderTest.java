package dev.haniel.menu.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.MenuFramework;
import dev.haniel.menu.paper.registry.ReloadFailure;
import dev.haniel.menu.paper.registry.ReloadReport;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class MenuReloaderTest {

  private final MenuMessages messages = mock(MenuMessages.class);
  private final MenuFramework framework = mock(MenuFramework.class);
  private final Logger logger = Logger.getLogger("MenuReloaderTest");
  private final MenuReloader reloader = new MenuReloader(messages, logger, () -> framework);

  @Test
  void deniesReloadWithoutPermission() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(false);

    reloader.reloadAll(player);

    verify(messages).send(player, "<red>You do not have permission to reload menus.</red>");
    verify(framework, never()).reloadAllReportAsync();
  }

  @Test
  void warnsWhenFrameworkNotAttached() {
    MenuReloader notReady = new MenuReloader(messages, logger, () -> null);
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);

    notReady.reloadAll(player);

    verify(messages).send(player, "<red>Menu framework is not ready.</red>");
  }

  @Test
  void reportsSuccessfulReload() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    when(player.isOnline()).thenReturn(true);
    ReloadReport report =
        new ReloadReport(List.of(new MenuId("main"), new MenuId("catalog")), List.of());
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));

    reloader.reloadAll(player);

    verify(messages).send(player, "<green>Reloaded 2 menu(s).</green>");
  }

  @Test
  void schedulesReloadReportOnPlayerSchedulerWhenPluginIsAvailable() {
    Plugin plugin = mock(Plugin.class);
    MenuReloader scheduled = new MenuReloader(messages, logger, plugin, () -> framework);
    Player player = mock(Player.class);
    UUID playerId = UUID.randomUUID();
    EntityScheduler scheduler = mock(EntityScheduler.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    when(player.getUniqueId()).thenReturn(playerId);
    when(player.getScheduler()).thenReturn(scheduler);
    when(player.isOnline()).thenReturn(true);
    ReloadReport report = new ReloadReport(List.of(new MenuId("main")), List.of());
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));

    scheduled.reloadAll(player);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Consumer<ScheduledTask>> task = ArgumentCaptor.forClass(Consumer.class);
    verify(scheduler).run(eq(plugin), task.capture(), any());
    verify(messages, never()).send(player, "<green>Reloaded 1 menu(s).</green>");

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(player);
      task.getValue().accept(mock(ScheduledTask.class));
    }

    verify(messages).send(player, "<green>Reloaded 1 menu(s).</green>");
  }

  @Test
  void aSchedulerThatRejectsTheReportDoesNotLogAReloadFailure() {
    Plugin plugin = mock(Plugin.class);
    MenuReloader scheduled = new MenuReloader(messages, logger, plugin, () -> framework);
    Player player = mock(Player.class);
    EntityScheduler scheduler = mock(EntityScheduler.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    when(player.getScheduler()).thenReturn(scheduler);
    when(scheduler.run(any(), any(), any()))
        .thenThrow(new IllegalStateException("player is no longer schedulable"));
    ReloadReport report = new ReloadReport(List.of(new MenuId("main")), List.of());
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));

    AtomicInteger severe = new AtomicInteger();
    Handler handler = capture(severe);
    logger.addHandler(handler);
    try {
      scheduled.reloadAll(player); // a scheduler rejection must not become a reload failure
    } finally {
      logger.removeHandler(handler);
    }

    assertEquals(
        0, severe.get(), "a successful reload must not log a failure when reporting fails");
    verify(messages, never()).send(eq(player), any());
  }

  @Test
  void reportsFailedReloadWithDetails() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    when(player.isOnline()).thenReturn(true);
    ReloadFailure failure = new ReloadFailure(new MenuId("catalog"), "broken yaml");
    ReloadReport report = new ReloadReport(List.of(), List.of(failure));
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));

    reloader.reloadAll(player);

    verify(messages).send(player, "<red>Reloaded with 1 failure(s).</red>");
    verify(messages).send(player, "<gray>- catalog: broken yaml</gray>");
  }

  @Test
  void logsAsyncReloadFailureInsteadOfSwallowingIt() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    when(framework.reloadAllReportAsync())
        .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));

    AtomicInteger severe = new AtomicInteger();
    Handler handler = capture(severe);
    logger.addHandler(handler);
    try {
      reloader.reloadAll(player); // must not propagate the async failure
    } finally {
      logger.removeHandler(handler);
    }

    assertEquals(1, severe.get(), "an async reload failure must be logged, not swallowed");
  }

  private static Handler capture(AtomicInteger severe) {
    return new Handler() {
      @Override
      public void publish(LogRecord record) {
        if (record.getLevel() == Level.SEVERE) {
          severe.incrementAndGet();
        }
      }

      @Override
      public void flush() {}

      @Override
      public void close() {}
    };
  }
}
