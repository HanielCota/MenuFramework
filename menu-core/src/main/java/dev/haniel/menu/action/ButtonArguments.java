package dev.haniel.menu.action;

import dev.haniel.menu.click.ClickContext;

/**
 * Produces the argument array passed to a {@code @Button} method handle when it fires.
 *
 * <p>A no-parameter button yields an empty array; a single-parameter button yields the one value
 * its {@link ClickArgumentResolver} resolves from the click. The shape is decided once per button
 * at boot, then applied per click — so the binding is a first-class value rather than an {@code
 * Optional} resolver threaded through fields and parameters.
 */
@FunctionalInterface
public interface ButtonArguments {

  /**
   * Resolves the arguments for the given click.
   *
   * @param context the click that triggered the button; never null
   * @return the arguments to invoke the button handle with; never null
   */
  Object[] forContext(ClickContext context);
}
