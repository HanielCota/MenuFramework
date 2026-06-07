package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import org.junit.jupiter.api.Test;

class StaticReaderTest {

  private final StaticReader reader = new StaticReader();

  @Test
  void readsIdAndButtonBehaviors() {
    MenuBlueprint blueprint = reader.read(new ValidMenu());

    assertEquals("valid", blueprint.id().value());
    assertEquals(2, blueprint.behaviors().size());
  }

  @Test
  void bindsPrivateButtonMethods() {
    MenuBlueprint blueprint = reader.read(new PrivateButtonMenu());

    assertEquals(1, blueprint.behaviors().size());
  }

  @Test
  void rejectsClassWithoutMenuAnnotation() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new NotAMenu()));
  }

  @Test
  void rejectsDuplicateButtonIds() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new DuplicateButtonMenu()));
  }

  @Menu(id = "valid")
  static final class ValidMenu {

    @Button(id = "one")
    void one() {}

    @Button(id = "two")
    void two() {}
  }

  @Menu(id = "private-buttons")
  static final class PrivateButtonMenu {

    @Button(id = "secret")
    private void secret() {}
  }

  static final class NotAMenu {

    @Button(id = "orphan")
    void orphan() {}
  }

  @Menu(id = "dupe")
  static final class DuplicateButtonMenu {

    @Button(id = "same")
    void first() {}

    @Button(id = "same")
    void second() {}
  }
}
