package dev.haniel.menu.paper.refresh;

import java.util.Set;
import org.bukkit.event.Event;

/**
 * Subscribes an open menu view to a set of Bukkit events, refreshing it when any of them fires.
 *
 * <p>Abstracted like {@code MenuScheduler} so the platform binding (real Bukkit event registration)
 * stays isolated and the open/close cycle is testable without a server.
 */
public interface RefreshSubscriber {

  /**
   * Subscribes to the given events, running {@code onFire} each time any of them fires.
   *
   * @param events the event types to listen for; never null, may be empty
   * @param onFire the action to run on each fire (typically a view refresh); never null
   * @return a {@link Runnable} that cancels the subscription; never null
   */
  Runnable subscribe(Set<Class<? extends Event>> events, Runnable onFire);

  /**
   * Returns a subscriber that registers nothing, for menus and tests without event refresh.
   *
   * @return a no-op subscriber whose cancel action does nothing
   */
  static RefreshSubscriber none() {
    return (events, onFire) -> () -> {};
  }
}
