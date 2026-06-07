package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PermissionGuardTest {

  @Test
  void runsTheActionWhenThePlayerHoldsThePermission() {
    int[] runs = {0};
    MenuAction guarded = PermissionGuard.require("menu.use", context -> runs[0]++);

    guarded.onClick(context(true));

    assertEquals(1, runs[0]);
  }

  @Test
  void skipsTheActionWhenThePlayerLacksThePermission() {
    int[] runs = {0};
    MenuAction guarded = PermissionGuard.require("menu.use", context -> runs[0]++);

    guarded.onClick(context(false));

    assertEquals(0, runs[0]);
  }

  private static ClickContext context(boolean permitted) {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
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
