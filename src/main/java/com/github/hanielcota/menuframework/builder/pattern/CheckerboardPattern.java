package com.github.hanielcota.menuframework.builder.pattern;

/**
 * Checkerboard pattern: every other slot.
 */
public final class CheckerboardPattern implements SlotPatternStrategy {

    @Override
    public boolean matches(int slot, int rows) {
        return (slot % 2) == 0;
    }
}
