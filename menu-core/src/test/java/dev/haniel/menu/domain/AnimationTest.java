package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AnimationTest {

  @Test
  void cyclesThroughFramesInOrder() {
    Animation<String> animation = Animation.of("a", "b", "c");

    assertEquals("a", animation.frame(0));
    assertEquals("b", animation.frame(1));
    assertEquals("c", animation.frame(2));
    assertEquals("a", animation.frame(3), "wraps back to the first frame after the last");
  }

  @Test
  void mapsNegativeStepsWithAFlooredModulo() {
    Animation<String> animation = Animation.of("a", "b", "c");

    assertEquals("c", animation.frame(-1));
    assertEquals("a", animation.frame(-3));
  }

  @Test
  void mapsVeryLargeStepsWithoutOverflow() {
    Animation<String> animation = Animation.of("a", "b");

    assertEquals("a", animation.frame(Long.MAX_VALUE - 1));
    assertEquals("b", animation.frame(Long.MAX_VALUE));
  }

  @Test
  void rejectsAnEmptyAnimation() {
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new Animation<>(List.of()));
    assertTrue(thrown.getMessage().contains("at least one frame"));
  }

  @Test
  void isImmutableAgainstTheSourceList() {
    List<String> source = new java.util.ArrayList<>(List.of("a", "b"));
    Animation<String> animation = new Animation<>(source);

    source.clear();

    assertEquals(2, animation.size());
    assertEquals("a", animation.frame(0));
  }
}
