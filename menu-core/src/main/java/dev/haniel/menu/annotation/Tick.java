package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a no-arg method as a periodic tick, run on a fixed schedule while the menu is open.
 *
 * <p>Only meaningful on a paginated (reactive) menu: a fresh instance is created per open, the tick
 * starts after the first {@link #period()} ticks and is cancelled when the view closes (no leak).
 * The method runs on the view's owning thread — the main thread on Paper, the player's region
 * thread on Folia — so it may safely update a {@code @Reactive State<?>}; that change drives the
 * usual coalesced re-render. Use it for countdowns in lore or frame-based animations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tick {

  /**
   * Returns how often the method runs, in server ticks ({@code 20} ticks is one second).
   *
   * @return the period in ticks; must be {@code >= 1}
   */
  int period() default 20;
}
