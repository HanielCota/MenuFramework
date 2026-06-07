package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a menu template, identified by a logical id.
 *
 * <p>The id is the only metadata carried here: appearance (title, size, slots) is resolved later
 * from configuration. Annotated classes are read once at boot by the compiler.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Menu {

  /**
   * Returns the logical id of the menu.
   *
   * @return the menu id; never blank
   */
  String id();

  /**
   * Returns the permission required to open this menu.
   *
   * <p>Empty means anyone may open it. When set, {@code open} silently does nothing for a player
   * lacking the permission, so callers wanting feedback should check the permission themselves.
   *
   * @return the permission node, or empty for no restriction
   */
  String permission() default "";
}
