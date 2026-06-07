package dev.haniel.menu.paper.argument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.compiler.model.ButtonBehavior;
import dev.haniel.menu.compiler.reader.ClickArguments;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class ButtonInjectionTest {

  private final StaticReader reader =
      new StaticReader(
          new ClickArguments(
              List.of(
                  new PlayerArgumentResolver(),
                  new MenuClickArgumentResolver(MiniMessage.miniMessage()))));

  @Test
  void injectsPlayerIntoButton() {
    Player player = mock(Player.class);
    InjectingMenu menu = new InjectingMenu();
    action(menu, "wants-player").onClick(context(player));
    assertSame(player, menu.receivedPlayer);
  }

  @Test
  void injectsMenuClickIntoButton() {
    Player player = mock(Player.class);
    InjectingMenu menu = new InjectingMenu();
    action(menu, "wants-click").onClick(context(player));
    assertSame(player, menu.receivedClick.player());
  }

  @Test
  void invokesNoArgButton() {
    InjectingMenu menu = new InjectingMenu();
    action(menu, "wants-nothing").onClick(context(mock(Player.class)));
    assertEquals(1, menu.noArgCalls);
  }

  private MenuAction action(InjectingMenu menu, String buttonId) {
    return reader.read(menu).behaviors().stream()
        .filter(behavior -> behavior.id().value().equals(buttonId))
        .map(ButtonBehavior::action)
        .findFirst()
        .orElseThrow();
  }

  private static PaperClickContext context(Player player) {
    return new PaperClickContext(new PlayerId(UUID.randomUUID()), ClickType.LEFT, player);
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
