package dev.haniel.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to run when a paginated menu closes for a player.
 *
 * <p>Invoked once when the inventory closes, on the view's owning thread (the main thread on Paper,
 * the player's region thread on Folia), so it may touch the Bukkit API. The method returns {@code
 * void} and either takes no arguments or a single {@code Player} (the viewer). Use it to save
 * per-player state or play a sound. Reactive state and ticks are already torn down for you.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClose {}
