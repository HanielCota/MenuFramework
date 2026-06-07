package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a clickable button, identified by a logical id.
 *
 * <p>The annotated method is the click handler. It is invoked through a {@code MethodHandle}
 * resolved once at boot. Slot and material are resolved later from configuration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Button {

  /**
   * Returns the logical id of the button.
   *
   * @return the button id; never blank
   */
  String id();
}
