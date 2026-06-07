package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code State<T>} field as reactive menu state.
 *
 * <p>Discovered once at boot. When a menu opens, every annotated field of the fresh per-player
 * instance is bound to that view, so changing the state re-renders the view automatically.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reactive {}
