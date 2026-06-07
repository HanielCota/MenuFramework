package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PagedAppearanceTest {

  private static MaskLayout twoRowLayout() {
    return MaskLayout.resolve(List.of("<XXXXXXX>", "#XXXXXXX#"), 2);
  }

  private static PagedAppearance<String> appearance(Map<Integer, String> overlays) {
    return new PagedAppearance<>(
        new MenuId("shop"),
        "<green>Shop",
        twoRowLayout(),
        new PagedDecor<>("prev", "next", "border"),
        overlays);
  }

  @Test
  void perPageMatchesTheNumberOfContentSlots() {
    PagedAppearance<String> appearance = appearance(Map.of());

    assertEquals(14, appearance.perPage());
  }

  @Test
  void sizeMatchesRowsTimesNine() {
    PagedAppearance<String> appearance = appearance(Map.of());

    assertEquals(18, appearance.size());
  }

  @Test
  void exposesTheGivenComponents() {
    PagedAppearance<String> appearance = appearance(Map.of(4, "button"));

    assertEquals(new MenuId("shop"), appearance.id());
    assertEquals("<green>Shop", appearance.title());
    assertEquals("button", appearance.overlayVisuals().get(4));
  }

  @Test
  void copiesTheOverlayMapDefensively() {
    Map<Integer, String> overlays = new HashMap<>(Map.of(4, "button"));
    PagedAppearance<String> appearance = appearance(overlays);

    overlays.put(5, "extra");

    assertEquals(1, appearance.overlayVisuals().size());
  }

  @Test
  void overlayMapIsUnmodifiable() {
    PagedAppearance<String> appearance = appearance(Map.of(4, "button"));

    assertThrows(
        UnsupportedOperationException.class, () -> appearance.overlayVisuals().put(5, "extra"));
  }
}
