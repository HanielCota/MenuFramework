package dev.haniel.menu.paper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.paper.facade.ManualFacadeMenu;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class MenuFrameworkBuilderTest {

  private static final String MENU_YAML =
      """
      title: "Test"
      rows: 3
      """;

  @Test
  void buildRegistersListenersOnceAndScansConfiguredPackages(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("alpha.yml"), MENU_YAML);
    Files.writeString(dir.resolve("bravo.yml"), MENU_YAML);
    PluginManager pluginManager = mock(PluginManager.class);

    MenuFramework framework =
        MenuFramework.builder(plugin(dir, pluginManager))
            .menusDirectory(dir)
            .scan("dev.haniel.menu.paper.samples")
            .build();

    // The click listener and the anvil-prompt listener, each registered exactly once.
    verify(pluginManager, times(2)).registerEvents(any(Listener.class), any(JavaPlugin.class));
    assertEquals(2, framework.reloadAll());
  }

  @Test
  void manualRegistrationAndScanCoexist(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("alpha.yml"), MENU_YAML);
    Files.writeString(dir.resolve("bravo.yml"), MENU_YAML);
    Files.writeString(dir.resolve("manual.yml"), MENU_YAML);

    MenuFramework framework =
        MenuFramework.builder(plugin(dir, mock(PluginManager.class)))
            .menusDirectory(dir)
            .scan("dev.haniel.menu.paper.samples")
            .build();

    framework.register(new ManualFacadeMenu());

    assertEquals(3, framework.reloadAll());
  }

  @Test
  void builderCanOnlyBuildOnce(@TempDir Path dir) {
    MenuFrameworkBuilder builder =
        MenuFramework.builder(plugin(dir, mock(PluginManager.class))).menusDirectory(dir);

    builder.build();

    assertThrows(IllegalStateException.class, builder::build);
  }

  @Test
  void shutdownTearsDownAnOpenReactiveViewEvenIfNoCloseEventFires(@TempDir Path dir) {
    JavaPlugin plugin = plugin(dir, mock(PluginManager.class));
    dev.haniel.menu.paper.reactive.ReactiveView view =
        mock(dev.haniel.menu.paper.reactive.ReactiveView.class);
    org.bukkit.entity.Player player = playerViewing(view);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(Bukkit::getScheduler).thenReturn(mock(BukkitScheduler.class));
      bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(player));

      new MenuLifecycle(plugin, List.of(mock(Listener.class)), Runnable::run).shutdown();
    }

    // shutdown unregisters listeners, so the close event may never fire — teardown must be
    // explicit.
    verify(view).close();
  }

  private static org.bukkit.entity.Player playerViewing(
      org.bukkit.inventory.InventoryHolder holder) {
    org.bukkit.entity.Player player = mock(org.bukkit.entity.Player.class);
    org.bukkit.inventory.InventoryView openView = mock(org.bukkit.inventory.InventoryView.class);
    org.bukkit.inventory.Inventory top = mock(org.bukkit.inventory.Inventory.class);
    when(player.getOpenInventory()).thenReturn(openView);
    when(openView.getTopInventory()).thenReturn(top);
    when(top.getHolder()).thenReturn(holder);
    return player;
  }

  @Test
  void shutdownCancelsPluginTasks(@TempDir Path dir) {
    JavaPlugin plugin = plugin(dir, mock(PluginManager.class));
    BukkitScheduler scheduler = mock(BukkitScheduler.class);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

      new MenuLifecycle(plugin, List.of(mock(Listener.class)), Runnable::run).shutdown();

      verify(scheduler).cancelTasks(plugin);
    }
  }

  @Test
  void shutdownSurvivesFoliaUnsupportedScheduler(@TempDir Path dir) {
    JavaPlugin plugin = plugin(dir, mock(PluginManager.class));
    BukkitScheduler scheduler = mock(BukkitScheduler.class);
    doThrow(new UnsupportedOperationException("Folia has no global scheduler"))
        .when(scheduler)
        .cancelTasks(plugin);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

      // On Folia the legacy scheduler throws; disable must still complete cleanly.
      assertDoesNotThrow(
          () -> new MenuLifecycle(plugin, List.of(mock(Listener.class)), Runnable::run).shutdown());
    }
  }

  @Test
  void syncExecutorDelegatesUntilShutdownThenRejects(@TempDir Path dir) {
    JavaPlugin plugin = plugin(dir, mock(PluginManager.class));
    int[] ran = {0};
    Executor delegate =
        command -> {
          ran[0]++;
          command.run();
        };
    MenuLifecycle lifecycle = new MenuLifecycle(plugin, List.of(mock(Listener.class)), delegate);

    lifecycle.syncExecutor().execute(() -> {});
    assertEquals(1, ran[0], "before shutdown the sync apply stage runs on the delegate");

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(Bukkit::getScheduler).thenReturn(mock(BukkitScheduler.class));
      lifecycle.shutdown();
    }

    assertThrows(
        RejectedExecutionException.class, () -> lifecycle.syncExecutor().execute(() -> {}));
    assertEquals(1, ran[0], "a late apply stage must not reach the delegate after shutdown");
  }

  @Test
  void scanRequiresAtLeastOnePackage(@TempDir Path dir) {
    MenuFrameworkBuilder builder =
        MenuFramework.builder(plugin(dir, mock(PluginManager.class))).menusDirectory(dir);

    assertThrows(IllegalArgumentException.class, builder::scan);
  }

  @Test
  void scanRejectsBlankPackage(@TempDir Path dir) {
    MenuFrameworkBuilder builder =
        MenuFramework.builder(plugin(dir, mock(PluginManager.class))).menusDirectory(dir);

    assertThrows(IllegalArgumentException.class, () -> builder.scan(" "));
  }

  @Test
  void scanRejectsNullPackageArray(@TempDir Path dir) {
    MenuFrameworkBuilder builder =
        MenuFramework.builder(plugin(dir, mock(PluginManager.class))).menusDirectory(dir);

    assertThrows(IllegalArgumentException.class, () -> builder.scan((String[]) null));
  }

  private JavaPlugin plugin(Path dataFolder, PluginManager pluginManager) {
    Server server = mock(Server.class);
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
    when(plugin.getServer()).thenReturn(server);
    when(plugin.getLogger()).thenReturn(Logger.getLogger("TestPlugin"));
    when(server.getPluginManager()).thenReturn(pluginManager);
    return plugin;
  }
}
