package com.github.hanielcota.menuframework.builder.pattern;

/**
 * Bottom row pattern: only slots in the last row.
 */
public final class BottomRowPattern implements SlotPatternStrategy {

    private static final int COLUMNS = 9;

    @Override
    public boolean matches(int slot, int rows) {
        return slot >= (rows - 1) * COLUMNS;
    }
}
