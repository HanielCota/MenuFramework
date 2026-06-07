package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Adversarial probes for {@link MaskLayout}: value-object contract, controls and shape. */
class MaskLayoutEdgeCasesTest {

  @Test
  void absentControlsAreMinusOne() {
    MaskLayout layout = MaskLayout.resolve(List.of("XXXXXXXXX"), 1);
    assertEquals(-1, layout.previousSlot());
    assertEquals(-1, layout.nextSlot());
  }

  @Test
  void contentSlotsAreInReadingOrderAcrossRows() {
    MaskLayout layout = MaskLayout.resolve(List.of("#X#######", "#######X#"), 2);
    assertArrayEquals(new int[] {1, 16}, layout.contentSlots());
  }

  @Test
  void rejectsMoreThanOneNextControl() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of(">XXXXXX>X"), 1));
  }

  @Test
  void rejectsLineWiderThanNine() {
    assertThrows(InvalidMenuException.class, () -> MaskLayout.resolve(List.of("XXXXXXXXXX"), 1));
  }

  @Test
  void singleContentSlotIsAccepted() {
    MaskLayout layout = MaskLayout.resolve(List.of("####X####"), 1);
    assertArrayEquals(new int[] {4}, layout.contentSlots());
  }

  @Test
  void layoutsFromIdenticalMasksAreEqual() {
    // A value object resolved twice from the same mask should be equal. Record
    // equals over int[] fields uses reference identity, so this fails.
    MaskLayout left = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);
    MaskLayout right = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);
    assertEquals(left, right);
  }

  @Test
  void equalLayoutsShareHashCode() {
    MaskLayout left = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);
    MaskLayout right = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);
    assertEquals(left.hashCode(), right.hashCode());
  }

  @Test
  void contentSlotCountMatchesContentSlotsWithoutExposingState() {
    MaskLayout layout = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);

    assertEquals(layout.contentSlots().length, layout.contentSlotCount());
    layout.contentSlots()[0] = 999;
    assertEquals(7, layout.contentSlotCount());
  }

  @Test
  void contentSlotsAccessorDoesNotExposeMutableInternalState() {
    // Mutating the returned array must not corrupt the value object's state.
    MaskLayout layout = MaskLayout.resolve(List.of("<XXXXXXX>"), 1);
    int[] firstView = layout.contentSlots();
    firstView[0] = 999;
    assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6, 7}, layout.contentSlots());
  }
}
