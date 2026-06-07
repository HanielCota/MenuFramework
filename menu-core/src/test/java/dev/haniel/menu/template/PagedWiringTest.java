package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PagedWiringTest {

  private static final MethodHandle NO_OP = MethodHandles.constant(Object.class, null);

  private static Instantiator instantiator() {
    Object instance = new Object();
    return new Instantiator(() -> instance);
  }

  private static UnboundProvider provider() {
    return new UnboundProvider(NO_OP);
  }

  private static UnboundAction action() {
    return new UnboundAction(NO_OP, context -> new Object[0]);
  }

  private static StateField stateField() {
    return new StateField("count", NO_OP);
  }

  @Test
  void exposesTheGivenComponents() {
    Instantiator instantiator = instantiator();
    UnboundProvider provider = provider();
    UnboundAction action = action();
    StateField field = stateField();
    PagedWiring wiring =
        new PagedWiring(instantiator, provider, Map.of(3, action), List.of(field));

    assertSame(instantiator, wiring.instantiator());
    assertSame(provider, wiring.provider());
    assertSame(action, wiring.overlayActions().get(3));
    assertSame(field, wiring.states().get(0));
  }

  @Test
  void copiesTheOverlayActionsDefensively() {
    Map<Integer, UnboundAction> actions = new HashMap<>(Map.of(3, action()));
    PagedWiring wiring = new PagedWiring(instantiator(), provider(), actions, List.of());

    actions.put(4, action());

    assertEquals(1, wiring.overlayActions().size());
  }

  @Test
  void copiesTheStatesDefensively() {
    List<StateField> states = new ArrayList<>(List.of(stateField()));
    PagedWiring wiring = new PagedWiring(instantiator(), provider(), Map.of(), states);

    states.clear();

    assertEquals(1, wiring.states().size());
  }

  @Test
  void overlayActionsAreUnmodifiable() {
    PagedWiring wiring =
        new PagedWiring(instantiator(), provider(), Map.of(3, action()), List.of());

    assertThrows(
        UnsupportedOperationException.class, () -> wiring.overlayActions().put(4, action()));
  }

  @Test
  void statesAreUnmodifiable() {
    PagedWiring wiring =
        new PagedWiring(instantiator(), provider(), Map.of(), List.of(stateField()));

    assertThrows(UnsupportedOperationException.class, () -> wiring.states().add(stateField()));
  }
}
