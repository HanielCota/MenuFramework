package dev.haniel.menu.paper.badsamples;

import dev.haniel.menu.annotation.Menu;

/** A discoverable menu with no usable no-arg constructor — instantiation must fail clearly. */
@Menu(id = "needsarg")
public class MenuNeedsArg {

  public MenuNeedsArg(int required) {
    // no no-arg constructor on purpose
  }
}
