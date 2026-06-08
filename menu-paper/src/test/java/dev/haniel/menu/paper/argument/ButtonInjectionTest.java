package dev.haniel.menu.paper.argument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.compiler.reader.ClickArguments;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.paper.api.MenuOpener;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ButtonInjectionTest {

  private final RecordingOpener opener = new RecordingOpener();
  private final StaticReader reader =
      new StaticReader(
          new ClickArguments(
              List.of(
                  new PlayerArgumentResolver(),
                  new MenuClickArgumentResolver(
                      MiniMessage.miniMessage(), opener, (viewer, prompt) -> {}))));

  @Test
  void injectsPlayerIntoButton() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      InjectingMenu menu = new InjectingMenu();
      action(menu, "wants-player")
          .onClick(new PaperClickContext(new PlayerId(uuid), ClickType.LEFT));
      assertSame(player, menu.receivedPlayer);
    }
  }

  @Test
  void injectsMenuClickIntoButton() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      InjectingMenu menu = new InjectingMenu();
      action(menu, "wants-click")
          .onClick(new PaperClickContext(new PlayerId(uuid), ClickType.LEFT));
      assertSame(player, menu.receivedClick.player());
    }
  }

  @Test
  void invokesNoArgButton() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      InjectingMenu menu = new InjectingMenu();
      action(menu, "wants-nothing")
          .onClick(new PaperClickContext(new PlayerId(uuid), ClickType.LEFT));
      assertEquals(1, menu.noArgCalls);
    }
  }

  @Test
  void injectedMenuClickOpensThroughTheConfiguredOpener() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      InjectingMenu menu = new InjectingMenu();
      action(menu, "wants-click")
          .onClick(new PaperClickContext(new PlayerId(uuid), ClickType.LEFT));

      menu.receivedClick.open(new MenuId("target"));

      assertSame(player, opener.openedFor);
      assertEquals("target", opener.openedId.value());
    }
  }

  private dev.haniel.menu.action.MenuAction action(InjectingMenu menu, String buttonId) {
    return reader.read(menu).behaviors().stream()
        .filter(behavior -> behavior.id().value().equals(buttonId))
        .map(dev.haniel.menu.compiler.model.ButtonBehavior::action)
        .findFirst()
        .orElseThrow();
  }

  /** Captures what a navigating {@link MenuClick} asked to open. */
  static final class RecordingOpener implements MenuOpener {
    Player openedFor;
    MenuId openedId;

    @Override
    public void open(Player viewer, MenuId id) {
      openedFor = viewer;
      openedId = id;
    }

    @Override
    public void open(Player viewer, Class<?> menuType) {
      openedFor = viewer;
    }
  }

  @Menu(id = "injecting")
  static final class InjectingMenu {
    Player receivedPlayer;
    MenuClick receivedClick;
    int noArgCalls;

    @Button(id = "wants-player")
    void player(Player player) {
      receivedPlayer = player;
    }

    @Button(id = "wants-click")
    void click(MenuClick click) {
      receivedClick = click;
    }

    @Button(id = "wants-nothing")
    void nothing() {
      noArgCalls++;
    }
  }
}
