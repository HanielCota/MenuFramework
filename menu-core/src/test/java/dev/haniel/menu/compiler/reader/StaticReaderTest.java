package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.annotation.Arg;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.OnOpen;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.state.State;
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

  @Test
  void rejectsInvalidMenuIdAsMenuError() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new InvalidMenuIdMenu()));
  }

  @Test
  void rejectsInvalidButtonIdAsMenuError() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new InvalidButtonIdMenu()));
  }

  @Test
  void rejectsLifecycleHookOnStaticMenu() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new StaticLifecycleMenu()));
  }

  @Test
  void rejectsTickOnStaticMenu() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new StaticTickMenu()));
  }

  @Test
  void rejectsReactiveStateOnStaticMenu() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new StaticReactiveMenu()));
  }

  @Test
  void rejectsViewerFieldOnStaticMenu() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new StaticViewerMenu()));
  }

  @Test
  void rejectsArgFieldOnStaticMenu() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new StaticArgMenu()));
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

  @Menu(id = "Bad")
  static final class InvalidMenuIdMenu {}

  @Menu(id = "invalid-button-id")
  static final class InvalidButtonIdMenu {

    @Button(id = " ")
    void blank() {}
  }

  @Menu(id = "static-lifecycle")
  static final class StaticLifecycleMenu {

    @OnOpen
    void open() {}
  }

  @Menu(id = "static-tick")
  static final class StaticTickMenu {

    @Tick
    void tick() {}
  }

  @Menu(id = "static-reactive")
  static final class StaticReactiveMenu {

    @Reactive private final State<Integer> count = State.of(0);
  }

  @Menu(id = "static-viewer")
  static final class StaticViewerMenu {

    @Viewer private PlayerId viewer;
  }

  @Menu(id = "static-arg")
  static final class StaticArgMenu {

    @Arg private String target;
  }
}
