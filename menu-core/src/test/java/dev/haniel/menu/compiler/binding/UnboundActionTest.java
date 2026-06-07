package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnboundActionTest {

  @Test
  void bindsHandleToInstanceAndRunsItOnClick() throws ReflectiveOperationException {
    UnboundAction unbound = new UnboundAction(unboundToggle(), ctx -> new Object[0]);
    Toggle toggle = new Toggle();

    MenuAction action = unbound.bind(toggle);
    action.onClick(context());

    assertTrue(toggle.fired);
  }

  @Test
  void bindsToTheGivenInstanceOnly() throws ReflectiveOperationException {
    UnboundAction unbound = new UnboundAction(unboundToggle(), ctx -> new Object[0]);
    Toggle bound = new Toggle();
    Toggle other = new Toggle();

    unbound.bind(bound).onClick(context());

    assertTrue(bound.fired);
    assertFalse(other.fired);
  }

  private static MethodHandle unboundToggle() throws ReflectiveOperationException {
    return MethodHandles.lookup().unreflect(Toggle.class.getDeclaredMethod("fire"));
  }

  private static ClickContext context() {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }

  static final class Toggle {

    boolean fired;

    void fire() {
      fired = true;
    }
  }
}
