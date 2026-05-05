package com.github.hanielcota.menuframework.definition;

import java.util.List;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;

public enum SlotPattern {
  FULL(rows -> IntStream.range(0, rows * 9).boxed().toList()),
  BORDERED(
      rows ->
          IntStream.range(1, rows - 1)
              .flatMap(row -> IntStream.range(1, 8).map(col -> row * 9 + col))
              .boxed()
              .toList()),
  CHEST_6(
      rows -> {
        int actualRows = Math.min(rows, 6);
        int start = (rows - actualRows) / 2;

        return IntStream.range(start, start + actualRows)
            .flatMap(row -> IntStream.range(0, 9).map(col -> row * 9 + col))
            .boxed()
            .toList();
      });

  private static final int MAX_ROWS = 6;

  private final java.util.function.IntFunction<List<Integer>> factory;

  SlotPattern(java.util.function.IntFunction<List<Integer>> factory) {
    this.factory = factory;
  }

  public @NonNull List<Integer> slots(int rows) {
    if (rows < 1 || rows > MAX_ROWS) {
      throw new IllegalArgumentException(
          "rows must be between 1 and " + MAX_ROWS + ", got: " + rows);
    }
    return factory.apply(rows);
  }

  public @NonNull List<Integer> slots() {
    return slots(MAX_ROWS);
  }
}
