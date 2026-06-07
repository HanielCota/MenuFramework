package dev.haniel.menu.action;

import dev.haniel.menu.click.ClickContext;

/**
 * Supplies the value for a single {@code @Button} method parameter from a click.
 *
 * <p>Register one resolver per injectable parameter type to widen the set of accepted
 * {@code @Button} signatures without editing the reader: teaching the framework a new parameter
 * type is a new resolver, never a new branch (Open/Closed). {@link ClickContext} is always
 * available; the platform layer contributes the rest (such as the Bukkit player).
 */
public interface ClickArgumentResolver {

  /**
   * Tells whether this resolver can supply a value for the given parameter type.
   *
   * @param parameterType the declared {@code @Button} parameter type; never null
   * @return {@code true} if {@link #resolve(ClickContext)} can produce a value for it
   */
  boolean supports(Class<?> parameterType);

  /**
   * Produces the argument value for the current click.
   *
   * @param context the click that triggered the button; never null
   * @return the value passed to the button method; never null
   */
  Object resolve(ClickContext context);
}
