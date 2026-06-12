package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the per-viewer visibility rule for a button.
 *
 * <p>The annotated method decides, for the player about to open the menu, whether the
 * {@code @Button} with the matching {@link #value() id} is shown. Returning {@code false} leaves
 * that button's slot empty and makes it non-clickable for that viewer; a button with no rule is
 * always shown.
 *
 * <p>The method must return {@code boolean} and take no arguments or a single Bukkit {@code Player}
 * (the viewer). Because it may accept a {@code Player}, it is read by the Paper layer via
 * reflection, not the core compiler. The {@link #value() id} must match an existing
 * {@code @Button(id = ...)}; an unknown id is a boot error. Works on static and paginated menus.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visible {

  /**
   * Returns the id of the {@code @Button} this rule controls.
   *
   * @return the button id; must match an existing {@code @Button}
   */
  String value();
}
