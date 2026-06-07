package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.paper.badsamples.MenuGood;
import dev.haniel.menu.paper.badsamples.MenuNeedsArg;
import org.junit.jupiter.api.Test;

class MenuInstantiatorTest {

  private final MenuInstantiator instances = new MenuInstantiator();

  @Test
  void createsMenuWithNoArgConstructor() {
    assertNotNull(instances.create(MenuGood.class));
  }

  @Test
  void failsWithClassNameWhenNoUsableConstructor() {
    InvalidMenuException failure =
        assertThrows(InvalidMenuException.class, () -> instances.create(MenuNeedsArg.class));
    assertTrue(failure.getMessage().contains("MenuNeedsArg"));
  }
}
