package dev.haniel.menu.item;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.MenuAction;
import org.junit.jupiter.api.Test;

class MenuItemTest {

  @Test
  void ofKeepsTheGivenIcon() {
    Icon icon = Icon.of("STONE");

    MenuItem item = MenuItem.of(icon);

    assertSame(icon, item.icon());
  }

  @Test
  void ofUsesANoOpActionThatDoesNotThrow() {
    MenuItem item = MenuItem.of(Icon.of("STONE"));

    item.action().onClick(null);
  }

  @Test
  void onClickReturnsANewItemCarryingTheAction() {
    MenuItem base = MenuItem.of(Icon.of("STONE"));
    MenuAction action = context -> {};

    MenuItem clicked = base.onClick(action);

    assertSame(action, clicked.action());
  }

  @Test
  void onClickKeepsTheOriginalIcon() {
    Icon icon = Icon.of("STONE");
    MenuItem base = MenuItem.of(icon);

    MenuItem clicked = base.onClick(context -> {});

    assertSame(icon, clicked.icon());
  }

  @Test
  void onClickLeavesTheOriginalItemUntouched() {
    MenuItem base = MenuItem.of(Icon.of("STONE"));
    MenuAction original = base.action();

    MenuItem clicked = base.onClick(context -> {});

    assertNotSame(clicked, base);
    assertSame(original, base.action());
  }

  @Test
  void ofRejectsNullIcon() {
    assertThrows(NullPointerException.class, () -> MenuItem.of(null));
  }

  @Test
  void onClickRejectsNullAction() {
    MenuItem base = MenuItem.of(Icon.of("STONE"));

    assertThrows(NullPointerException.class, () -> base.onClick(null));
  }
}
