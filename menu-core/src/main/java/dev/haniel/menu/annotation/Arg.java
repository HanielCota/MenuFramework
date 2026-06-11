package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the open argument into a paginated menu field before the first render.
 *
 * <p>When a menu is opened with {@code open(player, id, argument)}, the framework writes that
 * argument into every {@code @Arg} field of the fresh per-player instance whose declared type the
 * argument is assignable to, <em>before</em> the first {@code @Paginated} render runs. This lets a
 * menu be opened "for" a target, an amount or any typed context without smuggling it through
 * reactive state or a hand-rolled session carrier.
 *
 * <p>The annotated field must be a non-final, non-static reference type (never a primitive, which
 * cannot hold the absent value). When the menu is opened without an argument the field keeps its
 * default value. Opening with an argument that matches no {@code @Arg} field is a runtime error, so
 * a type mismatch fails loudly instead of leaving the field silently null.
 *
 * <p>Only valid on {@code @Paginated} menus, which own one instance per open; static menus share a
 * single instance across players and reject this annotation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {}
