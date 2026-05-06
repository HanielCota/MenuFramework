package com.github.hanielcota.menuframework.builder.pattern;

/** Strategy interface for determining whether a slot matches a predefined pattern. */
public interface SlotPatternStrategy {

  /** Returns true if the given slot matches this pattern for the specified number of rows. */
  boolean matches(int slot, int rows);
}
