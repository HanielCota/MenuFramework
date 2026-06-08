package dev.haniel.menu.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.api.MenuOpener;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

/** The boot-time bridge that forwards to the registry once it exists. */
class DeferredMenuOpenerTest {

  @Test
  void forwardsToTheDelegateOnceSet() {
    RecordingOpener target = new RecordingOpener();
    DeferredMenuOpener opener = new DeferredMenuOpener();
    opener.delegateTo(target);
    Player viewer = mock(Player.class);

    opener.open(viewer, new MenuId("target"));

    assertSame(viewer, target.openedFor);
    assertEquals("target", target.openedId.value());
  }

  @Test
  void throwsWhenUsedBeforeTheDelegateIsSet() {
    DeferredMenuOpener opener = new DeferredMenuOpener();

    assertThrows(
        IllegalStateException.class, () -> opener.open(mock(Player.class), new MenuId("target")));
  }

  private static final class RecordingOpener implements MenuOpener {
    private Player openedFor;
    private MenuId openedId;

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
}
