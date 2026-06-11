package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Arg;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.UnboundPageProvider;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.domain.Page;
import dev.haniel.menu.domain.PlayerId;
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
  void detectsLazyPageProvider() {
    assertInstanceOf(UnboundPageProvider.class, reader.read(LazyPageMenu.class).provider());
  }

  @Test
  void detectsEagerListProvider() {
    assertInstanceOf(UnboundProvider.class, reader.read(EagerListMenu.class).provider());
  }

  @Test
  void rejectsLazyProviderMissingThePageSizeParameter() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(OneIntLazyMenu.class));
    assertTrue(error.getMessage().contains("Page<MenuItem>"));
  }

  @Test
  void rejectsInvalidMenuIdAsMenuError() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(InvalidMenuIdMenu.class));
    assertTrue(error.getMessage().contains("@Menu id"));
  }

  @Test
  void rejectsInvalidButtonIdAsMenuError() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(InvalidButtonIdMenu.class));
    assertTrue(error.getMessage().contains("@Button id"));
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
  void rejectsViewerFieldThatIsNotPlayerId() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(BadViewerTypeMenu.class));
    assertTrue(error.getMessage().contains("must be PlayerId"));
  }

  @Test
  void rejectsFinalViewerField() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(FinalViewerMenu.class));
    assertTrue(error.getMessage().contains("non-final"));
  }

  @Test
  void acceptsViewerField() {
    PlayerId injected = new PlayerId(java.util.UUID.randomUUID());
    Object instance = reader.read(ViewerMenu.class).instantiator().create();
    reader.read(ViewerMenu.class).viewers().forEach(field -> field.inject(instance, injected));

    assertTrue(((ViewerMenu) instance).viewer == injected, "viewer must be written into the field");
  }

  @Test
  void acceptsArgField() {
    String passed = "target-player";
    Object instance = reader.read(ArgMenu.class).instantiator().create();
    reader.read(ArgMenu.class).args().forEach(field -> field.inject(instance, passed));

    assertTrue(
        ((ArgMenu) instance).target == passed, "the open argument must be written into @Arg");
  }

  @Test
  void argFieldAcceptsOnlyAssignableNonNullValues() {
    var field = reader.read(ArgMenu.class).args().getFirst();

    assertTrue(field.accepts("text"));
    assertFalse(field.accepts(42), "a value of the wrong type must not be accepted");
    assertFalse(field.accepts(null), "a missing value must not be accepted");
  }

  @Test
  void rejectsPrimitiveArgField() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(PrimitiveArgMenu.class));
    assertTrue(error.getMessage().contains("reference type"));
  }

  @Test
  void rejectsFinalArgField() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> reader.read(FinalArgMenu.class));
    assertTrue(error.getMessage().contains("non-final"));
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

  @Menu(id = "lazy")
  static final class LazyPageMenu {

    @Paginated
    Page<MenuItem> load(int page, int pageSize) {
      return Page.of(List.of(), false);
    }
  }

  @Menu(id = "eager-list")
  static final class EagerListMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "one-int-lazy")
  static final class OneIntLazyMenu {

    @Paginated
    Page<MenuItem> load(int page) {
      return Page.of(List.of(), false);
    }
  }

  @Menu(id = "Bad")
  static final class InvalidMenuIdMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "invalid-button-id")
  static final class InvalidButtonIdMenu {

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }

    @Button(id = " ")
    void blank() {}
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

  @Menu(id = "bad-viewer-type")
  static final class BadViewerTypeMenu {

    @Viewer String notPlayerId = "value";

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "final-viewer")
  static final class FinalViewerMenu {

    @Viewer final PlayerId viewer = null;

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "viewer")
  static final class ViewerMenu {

    @Viewer PlayerId viewer;

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "arg")
  static final class ArgMenu {

    @Arg String target;

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "primitive-arg")
  static final class PrimitiveArgMenu {

    @Arg int amount;

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }
  }

  @Menu(id = "final-arg")
  static final class FinalArgMenu {

    @Arg final String target = null;

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
