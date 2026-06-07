package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.util.List;
import org.junit.jupiter.api.Test;

class MaskLayoutTest {

  @Test
  void resolvesContentBorderAndNavigation() {
    MaskLayout layout = MaskLayout.resolve(List.of("<XXXXXXX>", "#########"), 2);
    assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6, 7}, layout.contentSlots());
    assertEquals(0, layout.previousSlot());
    assertEquals(8, layout.nextSlot());
    assertEquals(18, layout.size());
  }

  @Test
  void rejectsWrongWidth() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("XXXX"), 1));
  }

  @Test
  void rejectsHeightDifferentFromRows() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("XXXXXXXXX"), 2));
  }

  @Test
  void rejectsMaskWithoutContent() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("#########"), 1));
  }

  @Test
  void rejectsMoreThanOnePreviousControl() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("<XXXXXX<X"), 1));
  }

  @Test
  void rejectsUnknownMaskCharacter() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("@XXXXXXX@"), 1));
  }
}
