package dev.haniel.menu.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IconTest {

  @Test
  void ofHasNoNameOrLore() {
    Icon icon = Icon.of("STONE");

    assertEquals("STONE", icon.material());
    assertEquals("", icon.name());
    assertTrue(icon.lore().isEmpty());
  }

  @Test
  void namedReturnsNewInstanceLeavingOriginalUntouched() {
    Icon base = Icon.of("STONE");

    Icon named = base.named("<red>Hi");

    assertEquals("<red>Hi", named.name());
    assertEquals("", base.name());
  }

  @Test
  void describedByReplacesLore() {
    Icon icon = Icon.of("STONE").describedBy(List.of("line-a", "line-b"));

    assertEquals(List.of("line-a", "line-b"), icon.lore());
  }

  @Test
  void rejectsBlankMaterial() {
    assertThrows(IllegalArgumentException.class, () -> new Icon("  ", "n", List.of()));
  }

  @Test
  void copiesLoreDefensively() {
    List<String> lore = new ArrayList<>(List.of("line-a"));
    Icon icon = new Icon("STONE", "n", lore);

    lore.add("line-b");

    assertEquals(1, icon.lore().size());
  }
}
