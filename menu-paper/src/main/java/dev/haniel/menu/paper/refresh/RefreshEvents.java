package dev.haniel.menu.paper.refresh;

import dev.haniel.menu.paper.annotation.RefreshOn;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.event.Event;

/**
 * The {@code @RefreshOn} event types of a menu class, read once and cached.
 *
 * <p>Holds no per-instance state; the same set applies to every open of a given menu class.
 */
public final class RefreshEvents {

  private static final ConcurrentMap<Class<?>, Set<Class<? extends Event>>> CACHE =
      new ConcurrentHashMap<>();

  private RefreshEvents() {}

  /**
   * Returns the event types the given menu class refreshes on, reading them on first use.
   *
   * @param type the menu class; never null
   * @return the declared event types, or an empty set if the class has no {@code @RefreshOn}
   */
  public static Set<Class<? extends Event>> of(Class<?> type) {
    return CACHE.computeIfAbsent(type, RefreshEvents::read);
  }

  private static Set<Class<? extends Event>> read(Class<?> type) {
    RefreshOn annotation = type.getAnnotation(RefreshOn.class);
    if (annotation == null) {
      return Set.of();
    }
    return Set.copyOf(Arrays.asList(annotation.value()));
  }
}
