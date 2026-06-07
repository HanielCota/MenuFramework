package dev.haniel.menu.compiler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PagedStructureTest {

  @Test
  void exposesItsComponents() {
    PagedStructure structure =
        new PagedStructure(
            new MenuId("shop"), instantiator(), provider(), Map.of(), List.of());

    assertEquals("shop", structure.id().value());
    assertTrue(structure.buttons().isEmpty());
    assertTrue(structure.states().isEmpty());
  }

  @Test
  void copiesButtonsAndStatesDefensively() {
    Map<ButtonId, UnboundAction> buttons = new HashMap<>();
    List<StateField> states = new ArrayList<>();
    PagedStructure structure =
        new PagedStructure(new MenuId("shop"), instantiator(), provider(), buttons, states);

    buttons.put(new ButtonId("x"), null);
    states.add(null);

    assertTrue(structure.buttons().isEmpty());
    assertTrue(structure.states().isEmpty());
  }

  @Test
  void returnsImmutableCollections() {
    PagedStructure structure =
        new PagedStructure(new MenuId("shop"), instantiator(), provider(), Map.of(), List.of());

    assertThrows(
        UnsupportedOperationException.class,
        () -> structure.buttons().put(new ButtonId("x"), null));
  }

  private static Instantiator instantiator() {
    return new Instantiator(() -> new Object());
  }

  private static UnboundProvider provider() {
    return new UnboundProvider(itemsHandle());
  }

  private static MethodHandle itemsHandle() {
    try {
      return MethodHandles.lookup().unreflect(Source.class.getDeclaredMethod("items"));
    } catch (NoSuchMethodException | IllegalAccessException exception) {
      throw new IllegalStateException(exception);
    }
  }

  static final class Source {

    List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }
}
