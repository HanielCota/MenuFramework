package dev.haniel.menu.paper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.event.Event;

/**
 * Re-renders an open paginated menu whenever one of the listed Bukkit events fires.
 *
 * <p>While a player has the menu open, the framework subscribes to each listed event and, on fire,
 * re-renders the view exactly as {@code MenuFramework.session(player).refresh()} would — re-running
 * the {@code @Paginated} provider so the menu reflects data it reads but does not own (a balance
 * changed by another system, an admin action, a config edit). The subscription is removed when the
 * view closes, so there is nothing to unregister by hand.
 *
 * <p>This is the declarative counterpart to calling {@code refresh()} from your own listener: the
 * framework cannot forget to refresh, and there is no bridge to wire. Use {@code @Reactive} state
 * for data the menu <em>owns</em>; use {@code @RefreshOn} for external changes the menu only reads.
 *
 * <p>The refresh fires for every occurrence of the event regardless of which entity it concerns, on
 * the thread the event fires on; pair it with main-thread domain events. For per-target precision,
 * call {@code session(player).refresh()} from your own handler instead.
 *
 * <p>This annotation lives in the Paper layer because it references Bukkit event types. Valid only
 * on {@code @Paginated} menus; static menus reject it at boot.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshOn {

  /**
   * Returns the event types that trigger a re-render of the open menu.
   *
   * @return the Bukkit event classes to subscribe to; never empty in practice
   */
  Class<? extends Event>[] value();
}
