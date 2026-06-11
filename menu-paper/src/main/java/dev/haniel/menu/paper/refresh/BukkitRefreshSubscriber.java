package dev.haniel.menu.paper.refresh;

import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

/**
 * Registers Bukkit event handlers that refresh an open view, scoped to that view's lifetime.
 *
 * <p>Each subscription owns a private marker {@link Listener} so its handlers can be removed
 * independently when the view closes, with no shared state between views. Handlers run at {@link
 * EventPriority#MONITOR} (observe-only) and forward every fire to the view's refresh action.
 */
public final class BukkitRefreshSubscriber implements RefreshSubscriber {

  private final Plugin plugin;

  /**
   * Creates a subscriber that registers handlers under the given plugin.
   *
   * @param plugin the owning plugin; never null
   */
  public BukkitRefreshSubscriber(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public Runnable subscribe(Set<Class<? extends Event>> events, Runnable onFire) {
    Objects.requireNonNull(onFire, "onFire");
    Listener marker = new Listener() {};
    EventExecutor executor = (listener, event) -> onFire.run();
    events.forEach(type -> register(type, marker, executor));
    return () -> HandlerList.unregisterAll(marker);
  }

  private void register(Class<? extends Event> type, Listener marker, EventExecutor executor) {
    Bukkit.getPluginManager()
        .registerEvent(type, marker, EventPriority.MONITOR, executor, plugin, false);
  }
}
