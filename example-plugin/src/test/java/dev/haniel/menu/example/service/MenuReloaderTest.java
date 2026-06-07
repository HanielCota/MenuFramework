package dev.haniel.menu.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.MenuFramework;
import dev.haniel.menu.paper.registry.ReloadFailure;
import dev.haniel.menu.paper.registry.ReloadReport;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class MenuReloaderTest {

  private final MenuMessages messages = mock(MenuMessages.class);
  private final MenuFramework framework = mock(MenuFramework.class);
  private final Logger logger = Logger.getLogger("MenuReloaderTest");
  private final MenuReloader reloader = new MenuReloader(messages, logger);

  @Test
  void deniesReloadWithoutPermission() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(false);
    reloader.attach(framework);

    reloader.reloadAll(player);

    verify(messages).send(player, "<red>You do not have permission to reload menus.</red>");
    verify(framework, never()).reloadAllReportAsync();
  }

  @Test
  void warnsWhenFrameworkNotAttached() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);

    reloader.reloadAll(player);

    verify(messages).send(player, "<red>Menu framework is not ready.</red>");
  }

  @Test
  void reportsSuccessfulReload() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    ReloadReport report =
        new ReloadReport(List.of(new MenuId("main"), new MenuId("catalog")), List.of());
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));
    reloader.attach(framework);

    reloader.reloadAll(player);

    verify(messages).send(player, "<green>Reloaded 2 menu(s).</green>");
  }

  @Test
  void reportsFailedReloadWithDetails() {
    Player player = mock(Player.class);
    when(player.hasPermission("menuexample.reload")).thenReturn(true);
    ReloadFailure failure = new ReloadFailure(new MenuId("catalog"), "broken yaml");
    ReloadReport report = new ReloadReport(List.of(), List.of(failure));
    when(framework.reloadAllReportAsync()).thenReturn(CompletableFuture.completedFuture(report));
    reloader.attach(framework);

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
    reloader.attach(framework);

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
