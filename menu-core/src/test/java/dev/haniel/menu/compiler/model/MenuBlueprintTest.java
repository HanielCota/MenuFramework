package dev.haniel.menu.compiler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MenuBlueprintTest {

  @Test
  void exposesIdAndBehaviors() {
    ButtonBehavior behavior = new ButtonBehavior(new ButtonId("buy"), context -> {});
    MenuBlueprint blueprint = new MenuBlueprint(new MenuId("shop"), List.of(behavior));

    assertEquals("shop", blueprint.id().value());
    assertEquals(1, blueprint.behaviors().size());
  }

  @Test
  void copiesBehaviorsDefensively() {
    List<ButtonBehavior> source = new ArrayList<>();
    source.add(new ButtonBehavior(new ButtonId("buy"), context -> {}));
    MenuBlueprint blueprint = new MenuBlueprint(new MenuId("shop"), source);

    source.clear();

    assertEquals(1, blueprint.behaviors().size());
  }

  @Test
  void returnsAnImmutableBehaviorList() {
    MenuBlueprint blueprint = new MenuBlueprint(new MenuId("shop"), List.of());

    assertThrows(
        UnsupportedOperationException.class,
        () -> blueprint.behaviors().add(new ButtonBehavior(new ButtonId("x"), context -> {})));
  }
}
