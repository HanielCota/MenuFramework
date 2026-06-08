package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the viewing player's {@code PlayerId} into a paginated menu field before the first
 * render.
 *
 * <p>The annotated field must be a non-final, non-static {@code PlayerId}. When a menu opens, the
 * framework writes the viewer into every annotated field of the fresh per-player instance
 * <em>before </em> the first {@code @Paginated} render runs, so the provider already knows who it
 * renders for. This removes the need to smuggle the viewer through reactive state and avoids a
 * wasted first render with an unknown viewer.
 *
 * <p>Only valid on {@code @Paginated} menus, which own one instance per open; static menus share a
 * single instance across players and reject this annotation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Viewer {}
