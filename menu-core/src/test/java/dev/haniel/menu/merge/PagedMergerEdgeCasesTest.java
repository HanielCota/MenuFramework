package dev.haniel.menu.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.PaginationConfig;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Adversarial edge cases for {@link PagedMerger}: overlay/static-slot collisions, out-of-range
 * overlay slots and silent overwrites between competing overlay buttons.
 *
 * <p>The reference mask is {@code ["<XX XX>", "#XXXXXX #"]} over two rows, giving: slot 0 = prev
 * nav, slot 8 = next nav, slots 1-2 and 6-7 and 10-15 = content, slot 9 and 17 = border, slots 3-5
 * and 16 = empty (space). Empty slots are the only legal home for an overlay button.
 */
class PagedMergerEdgeCasesTest {

  private final PagedReader reader = new PagedReader();
  private final PagedMerger<String> merger = new PagedMerger<>(Icon::material);

  /**
   * Two distinct overlay buttons configured onto the SAME (empty) slot must not silently overwrite
   * each other in the overlay maps. A slot that shows one button but triggers another, or where one
   * button vanishes entirely, is a misconfiguration and should fail the boot.
   */
  @Test
  void rejectsTwoOverlayButtonsOnTheSameSlot() {
    int emptySlot = 3;
    ButtonConfig first = new ButtonConfig(emptySlot, "LEVER", "<gray>A</gray>", List.of());
    ButtonConfig second = new ButtonConfig(emptySlot, "TORCH", "<gray>B</gray>", List.of());
    MenuConfig collision =
        new MenuConfig("shop", 2, Map.of("first", first, "second", second), pagination());

    assertThrows(
        InvalidMenuException.class,
        () -> merger.merge(reader.read(PagedTwoButtons.class), collision));
  }

  /**
   * An overlay button whose slot lies outside the menu bounds is a YAML appearance error. The
   * merger documents {@link InvalidMenuException} for appearance mismatches, so an out-of-range
   * overlay slot must surface as that domain exception, not a raw {@link IllegalArgumentException}.
   */
  @Test
  void rejectsOverlaySlotBeyondMenuSizeAsMenuError() {
    int beyondTwoRows = 50;
    ButtonConfig toggle = new ButtonConfig(beyondTwoRows, "LEVER", "", List.of());
    MenuConfig outOfRange = new MenuConfig("shop", 2, Map.of("toggle", toggle), pagination());

    assertThrows(
        InvalidMenuException.class, () -> merger.merge(reader.read(PagedSample.class), outOfRange));
  }

  /**
   * An overlay button on a genuinely empty (space) slot of the mask is legal and must render. Pins
   * the intended boundary of the dynamic-slot guard: empty slots are free real estate.
   */
  @Test
  void allowsOverlayButtonOnEmptySlot() {
    int emptySlot = 3;
    ButtonConfig toggle = new ButtonConfig(emptySlot, "LEVER", "<gray>Toggle</gray>", List.of());
    MenuConfig config = new MenuConfig("shop", 2, Map.of("toggle", toggle), pagination());

    CompiledPagedMenu<String> compiled = merger.merge(reader.read(PagedSample.class), config);

    assertEquals("LEVER", compiled.appearance().overlayVisuals().get(emptySlot));
  }

  private static PaginationConfig pagination() {
    ButtonConfig nav = new ButtonConfig(0, "ARROW", "", List.of());
    return new PaginationConfig(List.of("<XX   XX>", "#XXXXXX #"), nav, nav);
  }

  @Menu(id = "paged")
  public static final class PagedSample {
    @Button(id = "toggle")
    public void toggle(ClickContext context) {}

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  @Menu(id = "paged-two")
  public static final class PagedTwoButtons {
    @Button(id = "first")
    public void first(ClickContext context) {}

    @Button(id = "second")
    public void second(ClickContext context) {}

    @Paginated
    public List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }
}
