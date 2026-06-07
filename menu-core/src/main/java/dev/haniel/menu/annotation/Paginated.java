package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method that supplies a menu's paginated content.
 *
 * <p>The method takes no arguments and returns a {@code List<MenuItem>}; each item carries its own
 * appearance and click action. It is bound to a {@code MethodHandle} once at boot and invoked per
 * render. At most one method per menu may carry this annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Paginated {}
