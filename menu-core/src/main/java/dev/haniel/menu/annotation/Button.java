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

  /**
   * Returns the permission required to trigger this button.
   *
   * <p>Empty means anyone who sees the menu may click. When set, a click by a player lacking the
   * permission is silently ignored.
   *
   * @return the permission node, or empty for no restriction
   */
  String permission() default "";

  /**
   * Returns the minimum delay between two accepted clicks of this button, per player.
   *
   * <p>{@code 0} disables the cooldown. While the cooldown is active a click is silently ignored.
   * The window is per player and per button; for a shared static menu it persists across reopens,
   * for a per-player paginated menu it resets when the view is reopened.
   *
   * @return the cooldown in milliseconds, or {@code 0} for none
   */
  long cooldownMillis() default 0;
}
