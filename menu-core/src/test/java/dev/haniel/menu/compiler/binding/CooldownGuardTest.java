package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CooldownGuardTest {

  private final AtomicLong now = new AtomicLong();

  @Test
  void runsThenBlocksWithinTheWindowAndRunsAgainAfter() {
    int[] runs = {0};
    PlayerId player = new PlayerId(UUID.randomUUID());
    MenuAction guarded = CooldownGuard.wrap(context -> runs[0]++, 1000, now::get);

    guarded.onClick(context(player)); // t=0 accepted
    now.set(500);
    guarded.onClick(context(player)); // within window, dropped
    now.set(1000);
    guarded.onClick(context(player)); // window elapsed, accepted

    assertEquals(2, runs[0]);
  }

  @Test
  void cooldownIsPerPlayer() {
    int[] runs = {0};
    MenuAction guarded = CooldownGuard.wrap(context -> runs[0]++, 1000, now::get);

    guarded.onClick(context(new PlayerId(UUID.randomUUID())));
    guarded.onClick(context(new PlayerId(UUID.randomUUID())));

    assertEquals(2, runs[0], "a second player must not inherit the first's cooldown");
  }

  private static ClickContext context(PlayerId player) {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return player;
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }
}
