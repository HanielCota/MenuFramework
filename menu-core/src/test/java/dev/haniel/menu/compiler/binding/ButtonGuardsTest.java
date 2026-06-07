package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ButtonGuardsTest {

  @Test
  void noneLeavesTheActionUnchanged() {
    MenuAction action = context -> {};

    assertSame(action, ButtonGuards.none().apply(action));
  }

  @Test
  void permissionBlocksAClickWithoutIt() {
    int[] runs = {0};
    MenuAction guarded = new ButtonGuards("menu.use", 0).apply(context -> runs[0]++);

    guarded.onClick(context(new PlayerId(UUID.randomUUID()), false));

    assertEquals(0, runs[0]);
  }

  @Test
  void cooldownDropsAnImmediateSecondClick() {
    int[] runs = {0};
    PlayerId player = new PlayerId(UUID.randomUUID());
    MenuAction guarded = new ButtonGuards("", 100_000).apply(context -> runs[0]++);

    guarded.onClick(context(player, true));
    guarded.onClick(context(player, true));

    assertEquals(1, runs[0]);
  }

  @Test
  void cooldownIsSharedAcrossRebindsSoReopeningDoesNotResetIt() {
    int[] runs = {0};
    PlayerId player = new PlayerId(UUID.randomUUID());
    ButtonGuards guards = new ButtonGuards("", 100_000);
    MenuAction firstOpen = guards.apply(context -> runs[0]++);
    MenuAction secondOpen = guards.apply(context -> runs[0]++);

    firstOpen.onClick(context(player, true));
    secondOpen.onClick(context(player, true)); // a reopen re-binds but must keep the same window

    assertEquals(1, runs[0], "reopening a menu must not reset a button's cooldown");
  }

  @Test
  void permissionIsCheckedBeforeCooldownSoADeniedClickDoesNotConsumeIt() {
    int[] runs = {0};
    PlayerId player = new PlayerId(UUID.randomUUID());
    MenuAction guarded = new ButtonGuards("menu.use", 100_000).apply(context -> runs[0]++);

    guarded.onClick(context(player, false)); // denied: must not start the cooldown
    guarded.onClick(context(player, true)); // first accepted click

    assertEquals(1, runs[0]);
  }

  private static ClickContext context(PlayerId player, boolean permitted) {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return player;
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }

      @Override
      public boolean hasPermission(String permission) {
        return permitted;
      }
    };
  }
}
