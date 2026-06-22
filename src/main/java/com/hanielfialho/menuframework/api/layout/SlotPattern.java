package com.hanielfialho.menuframework.api.layout;

import com.hanielfialho.menuframework.api.MenuLayout;

/** Resolves an ordered slot region for a concrete menu layout. */
@FunctionalInterface
public interface SlotPattern {

  /**
   * Resolves and validates this pattern.
   *
   * @param layout target layout
   * @return non-empty immutable region
   */
  SlotRegion resolve(MenuLayout layout);
}
