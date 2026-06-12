package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.state.State;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Boot-time validation of shapes that used to pass the readers and crash later, on every open:
 * static annotated members, abstract menu classes, compiler-generated bridge methods carrying
 * copied annotations, and re-annotated overrides surfacing as phantom duplicates.
 */
class BootValidationEdgeCasesTest {

  private final PagedReader pagedReader = new PagedReader();
  private final StaticReader staticReader = new StaticReader();

  // ---------------------------------------------------------------------------------------------
  // Static members must fail the boot, not every open
  // ---------------------------------------------------------------------------------------------

  @Test
  void rejectsStaticButtonOnPagedMenu() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> pagedReader.read(StaticButtonMenu.class));
    assertTrue(error.getMessage().contains("must not be static"));
  }

  @Test
  void rejectsStaticPaginatedProvider() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> pagedReader.read(StaticProviderMenu.class));
    assertTrue(error.getMessage().contains("must not be static"));
  }

  @Test
  void rejectsStaticTick() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> pagedReader.read(StaticTickMenu.class));
    assertTrue(error.getMessage().contains("must not be static"));
  }

  @Test
  void rejectsStaticReactiveField() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> pagedReader.read(StaticReactiveMenu.class));
    assertTrue(error.getMessage().contains("non-static"));
  }

  @Test
  void rejectsStaticButtonOnStaticMenu() {
    InvalidMenuException error =
        assertThrows(
            InvalidMenuException.class, () -> staticReader.read(new StaticButtonStaticMenu()));
    assertTrue(error.getMessage().contains("must not be static"));
  }

  @Test
  void rejectsAbstractPagedMenuAtBoot() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> pagedReader.read(AbstractPagedMenu.class));
    assertTrue(error.getMessage().contains("concrete class"));
  }

  // ---------------------------------------------------------------------------------------------
  // Bridge methods and overrides must not surface as phantom duplicates
  // ---------------------------------------------------------------------------------------------

  @Test
  void genericClickHandlerInterfaceBoots() {
    // javac copies @Button onto the press(Object) bridge; an unfiltered walk would reject the
    // bridge's Object parameter (or report a duplicate id) and a perfectly valid menu fails boot.
    PagedStructure structure = pagedReader.read(GenericHandlerMenu.class);
    assertEquals(1, structure.buttons().size());
  }

  @Test
  void reAnnotatedButtonOverrideIsOneButton() {
    PagedStructure structure = pagedReader.read(OverridingPagedMenu.class);
    assertEquals(1, structure.buttons().size());
  }

  @Test
  void reAnnotatedPaginatedOverrideIsOneProvider() {
    PagedStructure structure = pagedReader.read(OverridingProviderMenu.class);
    assertEquals(1, structure.buttons().size());
  }

  @Test
  void reAnnotatedButtonOverrideOnStaticMenuIsOneBehavior() {
    MenuBlueprint blueprint = staticReader.read(new OverridingStaticMenu());
    assertEquals(1, blueprint.behaviors().size());
  }

  // ---------------------------------------------------------------------------------------------
  // Fixtures
  // ---------------------------------------------------------------------------------------------

  @Menu(id = "static-button")
  static class StaticButtonMenu {
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }

    @Button(id = "x")
    static void press() {}
  }

  @Menu(id = "static-provider")
  static class StaticProviderMenu {
    @Paginated
    static List<MenuItem> content() {
      return List.of();
    }
  }

  @Menu(id = "static-tick")
  static class StaticTickMenu {
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }

    @Tick(period = 20)
    static void pulse() {}
  }

  @Menu(id = "static-reactive")
  static class StaticReactiveMenu {
    @Reactive static State<String> shared = State.of("a");

    @Paginated
    List<MenuItem> content() {
      return List.of();
    }
  }

  @Menu(id = "abstract-paged")
  abstract static class AbstractPagedMenu {
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }
  }

  @Menu(id = "static-button-static-menu")
  static class StaticButtonStaticMenu {
    @Button(id = "x")
    static void press() {}
  }

  interface Handler<C> {
    void press(C context);
  }

  @Menu(id = "generic-handler")
  static class GenericHandlerMenu implements Handler<ClickContext> {
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }

    @Override
    @Button(id = "x")
    public void press(ClickContext context) {}
  }

  @Menu(id = "paged-base")
  abstract static class PagedBase {
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }

    @Button(id = "x")
    public void press() {}
  }

  static class OverridingPagedMenu extends PagedBase {
    // Re-annotating an overridden handler with the same id is the idiomatic way to override a
    // base menu's button; it must read as one button, not a duplicate-id boot failure.
    @Override
    @Button(id = "x")
    public void press() {}
  }

  static class OverridingProviderMenu extends PagedBase {
    @Override
    @Paginated
    List<MenuItem> content() {
      return List.of();
    }
  }

  @Menu(id = "static-base")
  abstract static class StaticBase {
    @Button(id = "x")
    public void press() {}
  }

  static class OverridingStaticMenu extends StaticBase {
    @Override
    @Button(id = "x")
    public void press() {}
  }
}
