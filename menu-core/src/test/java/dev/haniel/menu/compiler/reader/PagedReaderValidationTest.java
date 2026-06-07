package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.item.MenuItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class PagedReaderValidationTest {

  private final PagedReader reader = new PagedReader();

  @Test
  void rejectsClassWithoutMenuAnnotation() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(NoMenuPaged.class));
    assertTrue(error.getMessage().contains("@Menu"));
  }

  @Test
  void rejectsMissingPaginatedProvider() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(NoProviderMenu.class));
    assertTrue(error.getMessage().contains("no @Paginated method"));
  }

  @Test
  void rejectsMoreThanOnePaginatedProvider() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(MultiProviderMenu.class));
    assertTrue(error.getMessage().contains("more than one @Paginated"));
  }

  @Test
  void rejectsProviderThatTakesArguments() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(ProviderWithArgsMenu.class));
    assertTrue(error.getMessage().contains("List<MenuItem>"));
  }

  @Test
  void rejectsProviderWithWrongReturnType() {
    assertThrows(InvalidMenuException.class, () -> reader.read(ProviderWrongReturnMenu.class));
  }

  @Test
  void rejectsProviderWithWrongListElement() {
    assertThrows(InvalidMenuException.class, () -> reader.read(ProviderWrongElementMenu.class));
  }

  @Test
  void rejectsDuplicateButtonIds() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(DuplicateButtonMenu.class));
    assertTrue(error.getMessage().contains("Duplicate @Button id"));
  }

  @Test
  void rejectsReactiveFieldThatIsNotState() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(BadReactiveMenu.class));
    assertTrue(error.getMessage().contains("must be State<?>"));
  }

  @Test
  void rejectsMenuWithoutNoArgConstructor() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(NoNoArgConstructorMenu.class));
    assertTrue(error.getMessage().contains("no-arg constructor"));
  }

  @Test
  void rejectsTickMethodWithArguments() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(TickWithArgsMenu.class));
    assertTrue(error.getMessage().contains("@Tick"));
  }

  @Test
  void rejectsTickWithNonPositivePeriod() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(ZeroPeriodTickMenu.class));
    assertTrue(error.getMessage().contains("period"));
  }

  static final class NoMenuPaged {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "no-provider")
  static final class NoProviderMenu {

    @Button(id = "x")
    void x() {}
  }

  @Menu(id = "multi-provider")
  static final class MultiProviderMenu {

    @Paginated
    List<MenuItem> first() {
      return List.of();
    }

    @Paginated
    List<MenuItem> second() {
      return List.of();
    }
  }

  @Menu(id = "provider-args")
  static final class ProviderWithArgsMenu {

    @Paginated
    List<MenuItem> items(int page) {
      return List.of();
    }
  }

  @Menu(id = "provider-return")
  static final class ProviderWrongReturnMenu {

    @Paginated
    String items() {
      return "";
    }
  }

  @Menu(id = "provider-element")
  static final class ProviderWrongElementMenu {

    @Paginated
    List<String> items() {
      return List.of();
    }
  }

  @Menu(id = "dup-button")
  static final class DuplicateButtonMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }

    @Button(id = "same")
    void first() {}

    @Button(id = "same")
    void second() {}
  }

  @Menu(id = "bad-reactive")
  static final class BadReactiveMenu {

    @Reactive String notState = "value";

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "no-ctor")
  static final class NoNoArgConstructorMenu {

    @SuppressWarnings("unused")
    NoNoArgConstructorMenu(String name) {}

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "tick-args")
  static final class TickWithArgsMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }

    @Tick
    void tick(int unused) {}
  }

  @Menu(id = "tick-zero")
  static final class ZeroPeriodTickMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }

    @Tick(period = 0)
    void tick() {}
  }
}
