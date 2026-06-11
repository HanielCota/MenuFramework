package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageTest {

  @Test
  void carriesItemsAndHasNext() {
    Page<String> page = Page.of(List.of("a", "b"), true);

    assertEquals(List.of("a", "b"), page.items());
    assertTrue(page.hasNext());
  }

  @Test
  void copiesItemsSoLaterMutationDoesNotLeakIn() {
    List<String> source = new ArrayList<>(List.of("a"));
    Page<String> page = Page.of(source, false);

    source.add("b");

    assertEquals(List.of("a"), page.items(), "the page must hold an immutable copy");
    assertFalse(page.hasNext());
  }

  @Test
  void rejectsNullItems() {
    assertThrows(NullPointerException.class, () -> Page.of(null, false));
  }
}
