package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dev.haniel.menu.domain.MenuId;
import org.junit.jupiter.api.Test;

class ReloadFailureTest {

  @Test
  void exposesIdAndMessage() {
    ReloadFailure failure = new ReloadFailure(new MenuId("shop"), "boom");

    assertEquals(new MenuId("shop"), failure.id());
    assertEquals("boom", failure.message());
  }

  @Test
  void equalsByIdAndMessage() {
    ReloadFailure first = new ReloadFailure(new MenuId("shop"), "boom");
    ReloadFailure second = new ReloadFailure(new MenuId("shop"), "boom");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void differsWhenMessageDiffers() {
    ReloadFailure first = new ReloadFailure(new MenuId("shop"), "boom");
    ReloadFailure second = new ReloadFailure(new MenuId("shop"), "bang");

    assertNotEquals(first, second);
  }
}
