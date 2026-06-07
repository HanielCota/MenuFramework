package dev.haniel.menu.action;

import dev.haniel.menu.click.ClickContext;

/**
 * The behaviour run when a slot is clicked.
 *
 * <p>Equivalent to a {@code Consumer<ClickContext>}, named for the domain so it reads at the call
 * site and can be referenced by the template and compiler.
 */
@FunctionalInterface
public interface MenuAction {

  /**
   * Runs this action for the given click.
   *
   * @param context the click that triggered the action; never null
   */
  void onClick(ClickContext context);
}
