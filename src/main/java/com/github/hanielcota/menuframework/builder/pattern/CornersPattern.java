package com.github.hanielcota.menuframework.builder.pattern;

/** Corners pattern: all four corners of the inventory. */
public final class CornersPattern implements SlotPatternStrategy {

  private static final int COLUMNS = 9;

  @Override
  public boolean matches(int slot, int rows) {
    return slot == 0
        || slot == COLUMNS - 1
        || slot == (rows - 1) * COLUMNS
        || slot == rows * COLUMNS - 1;
  }
}
