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
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StaticMergerTest {

  private final StaticReader reader = new StaticReader();
  private final StaticMerger<String> merger = new StaticMerger<>(Icon::material);

  @Test
  void rendersIconAndBindsActionAtConfiguredSlot() {
    SampleMenu menu = new SampleMenu();
    CompiledStaticMenu<String> compiled =
        (CompiledStaticMenu<String>) merger.merge(reader.read(menu), config(4));
    assertEquals("EMERALD", compiled.template().iconAt(4).orElseThrow());
    compiled.template().actionAt(4).orElseThrow().onClick(context());
    assertTrue(menu.bought);
  }

  @Test
  void failsWhenAnnotatedButtonHasNoConfigEntry() {
    MenuBlueprint blueprint = reader.read(new SampleMenu());
    MenuConfig empty = new MenuConfig("t", 1, Map.of(), null);
    assertThrows(InvalidMenuException.class, () -> merger.merge(blueprint, empty));
  }

  @Test
  void failsWhenButtonParameterIsNotInjectable() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new InvalidButtonMenu()));
  }

  @Test
  void bindsNoArgButton() {
    NoArgButtonMenu menu = new NoArgButtonMenu();
    CompiledStaticMenu<String> compiled =
        (CompiledStaticMenu<String>) merger.merge(reader.read(menu), config(4));
    compiled.template().actionAt(4).orElseThrow().onClick(context());
    assertTrue(menu.bought);
  }

  @Test
  void failsWhenButtonIdsAreDuplicated() {
    assertThrows(InvalidMenuException.class, () -> reader.read(new DuplicateButtonMenu()));
  }

  private static MenuConfig config(int slot) {
    ButtonConfig buy = new ButtonConfig(slot, "EMERALD", "<green>Buy</green>", List.of("line"));
    return new MenuConfig("title", 1, Map.of("buy", buy), null);
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

  @Menu(id = "sample")
  static final class SampleMenu {
    boolean bought;

    @Button(id = "buy")
    void buy(ClickContext context) {
      bought = true;
    }
  }

  @Menu(id = "invalid")
  static final class InvalidButtonMenu {
    @Button(id = "bad")
    void bad(String notInjectable) {}
  }

  @Menu(id = "no-arg")
  static final class NoArgButtonMenu {
    boolean bought;

    @Button(id = "buy")
    void buy() {
      bought = true;
    }
  }

  @Menu(id = "duplicate")
  static final class DuplicateButtonMenu {
    @Button(id = "same")
    void first(ClickContext context) {}

    @Button(id = "same")
    void second(ClickContext context) {}
  }
}
