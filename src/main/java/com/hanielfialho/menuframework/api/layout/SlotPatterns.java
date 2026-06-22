package com.hanielfialho.menuframework.api.layout;

import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Factory methods for common deterministic slot patterns. */
public final class SlotPatterns {

  private SlotPatterns() {}

  /**
   * Selects every slot in row-major order.
   *
   * @return all-slots pattern
   */
  public static SlotPattern all() {
    return layout -> {
      int[] slots = new int[layout.size()];
      for (int slot = 0; slot < layout.size(); slot++) {
        slots[slot] = slot;
      }
      return SlotRegion.of(layout, slots);
    };
  }

  /**
   * Selects explicit raw slots in the supplied order.
   *
   * @param slots raw slots
   * @return explicit pattern
   */
  public static SlotPattern slots(int... slots) {
    Objects.requireNonNull(slots, "slots");
    int[] snapshot = Arrays.copyOf(slots, slots.length);
    return layout -> SlotRegion.of(layout, snapshot);
  }

  /**
   * Selects one complete zero-based row.
   *
   * @param row zero-based row
   * @return row pattern
   */
  public static SlotPattern row(int row) {
    return layout -> {
      List<Integer> slots = new ArrayList<>(MenuLayout.CHEST_COLUMNS);
      for (int column = 0; column < MenuLayout.CHEST_COLUMNS; column++) {
        slots.add(layout.slot(row, column));
      }
      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Selects one complete zero-based column.
   *
   * @param column zero-based column
   * @return column pattern
   */
  public static SlotPattern column(int column) {
    return layout -> {
      List<Integer> slots = new ArrayList<>(layout.rows());
      for (int row = 0; row < layout.rows(); row++) {
        slots.add(layout.slot(row, column));
      }
      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Selects an inclusive rectangle in row-major order.
   *
   * @param firstRow first zero-based row
   * @param firstColumn first zero-based column
   * @param lastRow last zero-based row
   * @param lastColumn last zero-based column
   * @return rectangular pattern
   */
  public static SlotPattern rectangle(int firstRow, int firstColumn, int lastRow, int lastColumn) {
    if (firstRow > lastRow) {
      throw new IllegalArgumentException("firstRow cannot be greater than lastRow");
    }
    if (firstColumn > lastColumn) {
      throw new IllegalArgumentException("firstColumn cannot be greater than lastColumn");
    }

    return layout -> {
      List<Integer> slots = new ArrayList<>();
      for (int row = firstRow; row <= lastRow; row++) {
        for (int column = firstColumn; column <= lastColumn; column++) {
          slots.add(layout.slot(row, column));
        }
      }
      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Selects the outer border in clockwise-independent row-major order.
   *
   * @return border pattern
   */
  public static SlotPattern border() {
    return layout -> {
      LinkedHashSet<Integer> slots = new LinkedHashSet<>();
      int lastRow = layout.rows() - 1;
      int lastColumn = MenuLayout.CHEST_COLUMNS - 1;

      for (int row = 0; row < layout.rows(); row++) {
        for (int column = 0; column < MenuLayout.CHEST_COLUMNS; column++) {
          if (row == 0 || row == lastRow || column == 0 || column == lastColumn) {
            slots.add(layout.slot(row, column));
          }
        }
      }

      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Selects one half of a checkerboard.
   *
   * @param evenParity when {@code true}, selects coordinates whose row-plus-column sum is even
   * @return checkerboard pattern
   */
  public static SlotPattern checkerboard(boolean evenParity) {
    return layout -> {
      List<Integer> slots = new ArrayList<>();
      for (int row = 0; row < layout.rows(); row++) {
        for (int column = 0; column < MenuLayout.CHEST_COLUMNS; column++) {
          boolean even = ((row + column) & 1) == 0;
          if (even == evenParity) {
            slots.add(layout.slot(row, column));
          }
        }
      }
      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Resolves several patterns and returns their ordered union.
   *
   * @param first first pattern
   * @param remaining additional patterns
   * @return union pattern
   */
  public static SlotPattern union(SlotPattern first, SlotPattern... remaining) {
    Objects.requireNonNull(first, "first");
    Objects.requireNonNull(remaining, "remaining");
    SlotPattern[] snapshot = Arrays.copyOf(remaining, remaining.length);

    for (SlotPattern pattern : snapshot) {
      Objects.requireNonNull(pattern, "remaining contains null");
    }

    return layout -> {
      LinkedHashSet<Integer> slots = new LinkedHashSet<>();
      slots.addAll(first.resolve(layout).slots());
      for (SlotPattern pattern : snapshot) {
        slots.addAll(pattern.resolve(layout).slots());
      }
      return SlotRegion.copyOf(layout, slots);
    };
  }

  /**
   * Removes every slot selected by {@code excluded} from {@code source}.
   *
   * @param source source pattern
   * @param excluded slots to remove
   * @return subtraction pattern
   * @throws IllegalArgumentException if subtraction produces an empty region
   */
  public static SlotPattern excluding(SlotPattern source, SlotPattern excluded) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(excluded, "excluded");

    return layout -> {
      List<Integer> sourceSlots = source.resolve(layout).slots();
      Set<Integer> removed = Set.copyOf(excluded.resolve(layout).slots());
      List<Integer> result = sourceSlots.stream().filter(slot -> !removed.contains(slot)).toList();
      return SlotRegion.copyOf(layout, result);
    };
  }
}
