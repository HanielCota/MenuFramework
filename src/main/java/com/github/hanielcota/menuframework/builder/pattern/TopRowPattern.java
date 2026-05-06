package com.github.hanielcota.menuframework.builder.pattern;

/** Top row pattern: only slots in the first row. */
public final class TopRowPattern implements SlotPatternStrategy {

  private static final int COLUMNS = 9;

  @Override
  public boolean matches(int slot, int rows) {
    return slot < COLUMNS;
  }
}
