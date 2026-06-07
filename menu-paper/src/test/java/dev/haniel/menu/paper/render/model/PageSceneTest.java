package dev.haniel.menu.paper.render.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class PageSceneTest {

  @Test
  void perPageCountsContentSlotsOfTheLayout() {
    PageScene scene = scene(MaskLayout.resolve(List.of("XX#  <  >"), 1), 9);

    assertEquals(2, scene.perPage());
  }

  @Test
  void perPageReflectsAFullContentRow() {
    PageScene scene = scene(MaskLayout.resolve(List.of("XXXXXXXXX"), 1), 9);

    assertEquals(9, scene.perPage());
  }

  @Test
  void exposesItsComponents() {
    Component title = Component.text("Shop");
    MaskLayout layout = MaskLayout.resolve(List.of("X        "), 1);
    PageScene scene =
        new PageScene(
            new MenuId("shop"), title, 9, layout, null, null, new Overlay(Map.of(), Map.of()));

    assertEquals(new MenuId("shop"), scene.id());
    assertEquals(title, scene.title());
    assertEquals(9, scene.size());
    assertEquals(layout, scene.layout());
  }

  private static PageScene scene(MaskLayout layout, int size) {
    return new PageScene(
        new MenuId("shop"),
        Component.text("Shop"),
        size,
        layout,
        null,
        null,
        new Overlay(Map.of(), Map.of()));
  }
}
