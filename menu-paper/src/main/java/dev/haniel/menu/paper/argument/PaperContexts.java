package dev.haniel.menu.paper.argument;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.paper.listener.PaperClickContext;

/** Narrows a platform-neutral {@link ClickContext} to its Paper implementation. */
final class PaperContexts {

  private PaperContexts() {}

  static PaperClickContext require(ClickContext context) {
    if (context instanceof PaperClickContext paper) {
      return paper;
    }
    throw new IllegalStateException("This menu argument is only available on Paper");
  }
}
