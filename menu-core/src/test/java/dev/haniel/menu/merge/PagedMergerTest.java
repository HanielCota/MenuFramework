package dev.haniel.menu.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.PaginationConfig;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.state.State;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PagedMergerTest {

  private final PagedReader reader = new PagedReader();
  private final PagedMerger<String> merger = new PagedMerger<>(Icon::material);

  @Test
  void compilesPaginatedMenuAndBindsProviderToInstance() {
    CompiledPagedMenu<String> compiled = merger.merge(reader.read(PagedSample.class), config());
    assertEquals(14, compiled.appearance().perPage());
    assertEquals("ARROW", compiled.appearance().decor().previous());
    Object instance = compiled.wiring().instantiator().create();
    assertEquals(3, compiled.wiring().provider().bind(instance).provide().size());
  }

  @Test
  void discoversStateFields() {
    CompiledPagedMenu<String> compiled = merger.merge(reader.read(PagedSample.class), config());
    assertEquals(1, compiled.wiring().states().size());
  }

  @Test
  void rendersAndBindsOverlayButton() {
    CompiledPagedMenu<String> compiled = merger.merge(reader.read(PagedSample.class), config());
    assertEquals("LEVER", compiled.appearance().overlayVisuals().get(9));
    PagedSample instance = (PagedSample) compiled.wiring().instantiator().create();
    compiled.wiring().overlayActions().get(9).bind(instance).onClick(context());
    assertTrue(instance.toggled);
  }

  @Test
  void supportsCustomInstantiatorForPagedMenusWithoutNoArgConstructor() {
    PagedStructure structure =
        reader.read(
            PagedNeedsDependency.class, new Instantiator(() -> new PagedNeedsDependency("ok")));
    CompiledPagedMenu<String> compiled = merger.merge(structure, noButtonsConfig());

    Object instance = compiled.wiring().instantiator().create();

    assertEquals(
        "ok", compiled.wiring().provider().bind(instance).provide().getFirst().icon().name());
  }

  @Test
  void rejectsOverlayButtonOnContentSlot() {
    ButtonConfig toggle = new ButtonConfig(10, "LEVER", "<gray>Toggle</gray>", List.of());
    MenuConfig invalid = new MenuConfig("shop", 2, Map.of("toggle", toggle), pagination());

    assertThrows(
        InvalidMenuException.class, () -> merger.merge(reader.read(PagedSample.class), invalid));
  }

  @Test
  void rejectsOverlayButtonOnNavigationSlot() {
    ButtonConfig toggle = new ButtonConfig(0, "LEVER", "<gray>Toggle</gray>", List.of());
    MenuConfig invalid = new MenuConfig("shop", 2, Map.of("toggle", toggle), pagination());

    assertThrows(
        InvalidMenuException.class, () -> merger.merge(reader.read(PagedSample.class), invalid));
  }

  @Test
  void failsWhenPaginatedMenuHasNoPaginationConfig() {
    PagedStructure structure = reader.read(PagedSample.class);
    MenuConfig noPagination = new MenuConfig("t", 2, Map.of(), null);
    assertThrows(InvalidMenuException.class, () -> merger.merge(structure, noPagination));
  }

  @Test
  void compilesWhenPaginationMaskOmitsNavigationControls() {
    PaginationConfig pagination =
        new PaginationConfig(List.of("#XXXXXXX#", "#XXXXXXX#"), null, null);
    MenuConfig noNavigation = new MenuConfig("shop", 2, Map.of(), pagination);

    CompiledPagedMenu<String> compiled =
        merger.merge(reader.read(PagedWithoutButtons.class), noNavigation);

    assertNull(compiled.appearance().decor().previous());
    assertNull(compiled.appearance().decor().next());
  }

  @Test
  void failsWhenPagedButtonSignatureIsInvalid() {
    assertThrows(InvalidMenuException.class, () -> reader.read(PagedInvalidButton.class));
  }

  @Test
  void failsWhenPagedButtonIdsAreDuplicated() {
    assertThrows(InvalidMenuException.class, () -> reader.read(PagedDuplicateButtons.class));
  }

  @Test
  void failsWhenMoreThanOnePaginatedProviderExists() {
    assertThrows(InvalidMenuException.class, () -> reader.read(PagedDuplicateProviders.class));
  }

  @Test
  void failsWhenReactiveFieldIsNotState() {
    assertThrows(InvalidMenuException.class, () -> reader.read(PagedInvalidReactiveField.class));
  }

  @Test
  void failsClearlyWhenReactiveFieldIsNull() {
    CompiledPagedMenu<String> compiled =
        merger.merge(reader.read(PagedNullReactiveField.class), noButtonsConfig());
    Object instance = compiled.wiring().instantiator().create();

    assertThrows(
        InvalidMenuException.class, () -> compiled.wiring().states().getFirst().read(instance));
  }

  @Test
  void failsWhenProviderElementTypeIsNotMenuItem() {
    assertThrows(InvalidMenuException.class, () -> reader.read(PagedWrongItemType.class));
  }

  @Test
  void failsClearlyWhenProviderReturnsNull() {
    CompiledPagedMenu<String> compiled =
        merger.merge(reader.read(PagedNullProvider.class), config());
    Object instance = compiled.wiring().instantiator().create();

    assertThrows(
        MenuActionException.class, () -> compiled.wiring().provider().bind(instance).provide());
  }

  private static MenuConfig config() {
    ButtonConfig nav = new ButtonConfig(0, "ARROW", "", List.of());
    ButtonConfig toggle = new ButtonConfig(9, "LEVER", "<gray>Toggle</gray>", List.of());
    PaginationConfig pagination = pagination();
    return new MenuConfig("shop", 2, Map.of("toggle", toggle), pagination);
  }

  private static MenuConfig noButtonsConfig() {
    return new MenuConfig("shop", 2, Map.of(), pagination());
  }

  private static PaginationConfig pagination() {
    ButtonConfig nav = new ButtonConfig(0, "ARROW", "", List.of());
    return new PaginationConfig(List.of("<XXXXXXX>", "#XXXXXXX#"), nav, nav);
  }

  private static ClickContext context() {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }

  @Menu(id = "paged")
  public static final class PagedSample {
    @Reactive private final State<String> filter = State.of("all");

    boolean toggled;

    @Button(id = "toggle")
    public void toggle(ClickContext context) {
      toggled = true;
    }

    @Paginated
    public List<MenuItem> items() {
      return List.of(entry("A"), entry("B"), entry("C"));
    }

    private static MenuItem entry(String name) {
      return MenuItem.of(Icon.of("STONE").named(name)).onClick(context -> {});
    }
  }

  @Menu(id = "no-nav")
  public static final class PagedWithoutButtons {
    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "needs-dependency")
  public static final class PagedNeedsDependency {
    private final String dependency;

    public PagedNeedsDependency(String dependency) {
      this.dependency = dependency;
    }

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE").named(dependency)));
    }
  }

  @Menu(id = "bad-paged-button")
  public static final class PagedInvalidButton {
    @Button(id = "bad")
    public void bad(String notInjectable) {}

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "duplicate-paged-button")
  public static final class PagedDuplicateButtons {
    @Button(id = "same")
    public void first(ClickContext context) {}

    @Button(id = "same")
    public void second(ClickContext context) {}

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "duplicate-providers")
  public static final class PagedDuplicateProviders {
    @Paginated
    public List<MenuItem> first() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }

    @Paginated
    public List<MenuItem> second() {
      return List.of(MenuItem.of(Icon.of("DIRT")));
    }
  }

  @Menu(id = "wrong-item-type")
  public static final class PagedWrongItemType {
    @Paginated
    public List<String> items() {
      return List.of("not a menu item");
    }
  }

  @Menu(id = "bad-reactive-field")
  public static final class PagedInvalidReactiveField {
    @Reactive private final String filter = "all";

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "null-reactive")
  public static final class PagedNullReactiveField {
    @Reactive private final State<String> filter = null;

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "null-provider")
  public static final class PagedNullProvider {
    @Button(id = "toggle")
    public void toggle(ClickContext context) {}

    @Paginated
    public List<MenuItem> items() {
      return null;
    }
  }
}
