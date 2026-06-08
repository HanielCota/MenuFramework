package dev.haniel.menu.paper.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.annotation.OnClose;
import dev.haniel.menu.annotation.OnOpen;
import dev.haniel.menu.compiler.InvalidMenuException;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class HookDefinitionsTest {

  @Test
  void firesNoArgAndPlayerHandlers() {
    Recorder recorder = new Recorder();
    Player player = mock(Player.class);
    MenuHooks hooks = HookDefinitions.of(Recorder.class).bind(recorder);

    hooks.fireOpen(player);
    hooks.fireClose(player);

    assertEquals(1, recorder.opens);
    assertEquals(1, recorder.closes);
    assertSame(player, recorder.lastClosedFor);
  }

  @Test
  void aClassWithoutHooksFiresNothing() {
    MenuHooks hooks = HookDefinitions.of(NoHooks.class).bind(new NoHooks());

    hooks.fireOpen(mock(Player.class));
    hooks.fireClose(mock(Player.class));
  }

  @Test
  void rejectsAHandlerWithAnUnsupportedSignature() {
    assertThrows(InvalidMenuException.class, () -> HookDefinitions.of(BadHook.class));
  }

  @Test
  void rejectsAHandlerWithPlayerSupertype() {
    assertThrows(InvalidMenuException.class, () -> HookDefinitions.of(WideHook.class));
  }

  @Test
  void anOfflineViewerRunsNoArgCloseButSkipsPlayerClose() {
    CloseRecorder recorder = new CloseRecorder();
    MenuHooks hooks = HookDefinitions.of(CloseRecorder.class).bind(recorder);

    hooks.fireClose(null);

    assertEquals(1, recorder.noArgCloses, "a no-arg @OnClose runs even without an online viewer");
    assertEquals(0, recorder.playerCloses, "a Player @OnClose is skipped when the viewer is gone");
  }

  static final class Recorder {
    int opens;
    int closes;
    Player lastClosedFor;

    @OnOpen
    void opened() {
      opens++;
    }

    @OnClose
    void closed(Player player) {
      closes++;
      lastClosedFor = player;
    }
  }

  static final class CloseRecorder {
    int noArgCloses;
    int playerCloses;

    @OnClose
    void closedNoArg() {
      noArgCloses++;
    }

    @OnClose
    void closedWithPlayer(Player player) {
      playerCloses++;
    }
  }

  static final class NoHooks {}

  static final class BadHook {

    @OnOpen
    void opened(int unsupported) {}
  }

  static final class WideHook {

    @OnOpen
    void opened(Object player) {}
  }
}
