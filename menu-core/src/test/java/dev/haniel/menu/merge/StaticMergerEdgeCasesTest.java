package dev.haniel.menu.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Adversarial edge cases for {@link StaticMerger}: slot collisions, out-of-range slots and
 * config/annotation mismatches that should be boot errors but may slip through silently.
 */
class StaticMergerEdgeCasesTest {

  private final StaticReader reader = new StaticReader();
  private final StaticMerger<String> merger = new StaticMerger<>(Icon::material);

  /**
   * Two distinct annotated buttons configured onto the SAME slot must not silently overwrite each
   * other. A menu where clicking one slot can only ever trigger one of two declared behaviours is a
   * misconfiguration and should fail the boot with a clear error.
   */
  @Test
  void rejectsTwoButtonsConfiguredOnTheSameSlot() {
    ButtonConfig first = new ButtonConfig(4, "EMERALD", "<green>First</green>", List.of());
    ButtonConfig second = new ButtonConfig(4, "DIAMOND", "<aqua>Second</aqua>", List.of());
    MenuConfig collision = new MenuConfig("t", 1, Map.of("first", first, "second", second), null);

    assertThrows(
        InvalidMenuException.class,
        () -> merger.merge(reader.read(new TwoButtonMenu()), collision));
  }

  /**
   * A configured slot outside the menu bounds is a YAML appearance error. The merger documents
   * {@link InvalidMenuException} for behaviour/appearance mismatches, so an out-of-range slot must
   * surface as that domain exception rather than leaking a raw {@link IllegalArgumentException}.
   */
  @Test
  void rejectsButtonSlotBeyondMenuSizeAsMenuError() {
    ButtonConfig buy = new ButtonConfig(50, "EMERALD", "<green>Buy</green>", List.of());
    MenuConfig outOfRange = new MenuConfig("t", 1, Map.of("buy", buy), null);

    assertThrows(
        InvalidMenuException.class,
        () -> merger.merge(reader.read(new OneButtonMenu()), outOfRange));
  }

  /**
   * A button slot on the very last valid index (size - 1) must render and bind there, guarding
   * against an off-by-one in the {@code rows * 9} bounds.
   */
  @Test
  void bindsButtonOnLastSlotOfMenu() {
    OneButtonMenu menu = new OneButtonMenu();
    ButtonConfig buy = new ButtonConfig(8, "EMERALD", "<green>Buy</green>", List.of());
    MenuConfig config = new MenuConfig("t", 1, Map.of("buy", buy), null);

    CompiledStaticMenu<String> compiled =
        (CompiledStaticMenu<String>) merger.merge(reader.read(menu), config);

    assertEquals("EMERALD", compiled.template().iconAt(8).orElseThrow());
    compiled.template().actionAt(8).orElseThrow().onClick(context());
    assertTrue(menu.bought);
  }

  /**
   * A config button that is NOT annotated may still be rendered as decoration, but it must never
   * receive a click binding meant for a real behaviour. Verifies the decorative-only config button
   * shows its icon yet carries no action.
   */
  @Test
  void rendersUnboundConfigOnlyButtonWithoutAction() {
    OneButtonMenu menu = new OneButtonMenu();
    ButtonConfig buy = new ButtonConfig(0, "EMERALD", "", List.of());
    ButtonConfig decoration = new ButtonConfig(1, "BARRIER", "", List.of());
    MenuConfig config = new MenuConfig("t", 1, Map.of("buy", buy, "decor", decoration), null);

    CompiledStaticMenu<String> compiled =
        (CompiledStaticMenu<String>) merger.merge(reader.read(menu), config);

    assertEquals("BARRIER", compiled.template().iconAt(1).orElseThrow());
    assertTrue(compiled.template().actionAt(1).isEmpty());
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

  @Menu(id = "two-buttons")
  static final class TwoButtonMenu {
    boolean first;
    boolean second;

    @Button(id = "first")
    void first(ClickContext context) {
      first = true;
    }

    @Button(id = "second")
    void second(ClickContext context) {
      second = true;
    }
  }

  @Menu(id = "one-button")
  static final class OneButtonMenu {
    boolean bought;

    @Button(id = "buy")
    void buy(ClickContext context) {
      bought = true;
    }
  }
}
