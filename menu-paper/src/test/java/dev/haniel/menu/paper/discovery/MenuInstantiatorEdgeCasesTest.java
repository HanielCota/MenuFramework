package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import org.junit.jupiter.api.Test;

/** Adversarial edge cases for {@link MenuInstantiator}: abstract classes, throwing constructors. */
class MenuInstantiatorEdgeCasesTest {

  private final MenuInstantiator instances = new MenuInstantiator();

  /** An abstract class has a no-arg constructor but cannot be instantiated; must fail clearly. */
  @Test
  void abstractClassFailsWithClassName() {
    InvalidMenuException failure =
        assertThrows(InvalidMenuException.class, () -> instances.create(AbstractMenu.class));

    assertTrue(failure.getMessage().contains("AbstractMenu"));
  }

  /** An interface cannot be instantiated; must fail clearly naming the type. */
  @Test
  void interfaceFailsWithClassName() {
    InvalidMenuException failure =
        assertThrows(InvalidMenuException.class, () -> instances.create(MenuInterface.class));

    assertTrue(failure.getMessage().contains("MenuInterface"));
  }

  /** A no-arg constructor that throws must surface as a clear boot error, never a raw cause. */
  @Test
  void throwingConstructorFailsWithClassName() {
    InvalidMenuException failure =
        assertThrows(InvalidMenuException.class, () -> instances.create(ExplodingMenu.class));

    assertTrue(failure.getMessage().contains("ExplodingMenu"));
  }

  /** A private no-arg constructor must still be usable (setAccessible). */
  @Test
  void privateNoArgConstructorIsUsable() {
    assertTrue(instances.create(PrivateCtorMenu.class) instanceof PrivateCtorMenu);
  }

  abstract static class AbstractMenu {}

  interface MenuInterface {}

  static final class ExplodingMenu {
    ExplodingMenu() {
      throw new IllegalStateException("constructor boom");
    }
  }

  static final class PrivateCtorMenu {
    private PrivateCtorMenu() {}
  }
}
