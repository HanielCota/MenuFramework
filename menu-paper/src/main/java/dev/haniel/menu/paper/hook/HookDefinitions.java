package dev.haniel.menu.paper.hook;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 * The {@code @OnOpen}/{@code @OnClose} handlers of a menu class, resolved once and cached.
 *
 * <p>Resolution lives in {@link HookReader}; this type holds the resolved handlers, caches them per
 * class and binds them to a fresh instance per open via {@link #bind(Object)}.
 */
public final class HookDefinitions {

  private static final ConcurrentMap<Class<?>, HookDefinitions> CACHE = new ConcurrentHashMap<>();

  private final List<Handler> onOpen;
  private final List<Handler> onClose;

  HookDefinitions(List<Handler> onOpen, List<Handler> onClose) {
    this.onOpen = Objects.requireNonNull(onOpen, "onOpen");
    this.onClose = Objects.requireNonNull(onClose, "onClose");
  }

  /**
   * Returns the cached handlers for the given menu class, reading them on first use.
   *
   * @param type the menu class; never null
   * @return the resolved handlers
   * @throws InvalidMenuException if a handler has an unsupported signature
   */
  public static HookDefinitions of(Class<?> type) {
    return CACHE.computeIfAbsent(type, HookReader::read);
  }

  /**
   * Binds the handlers to the given instance, ready to fire.
   *
   * @param instance the per-player menu instance; never null
   * @return the bound hooks
   */
  public MenuHooks bind(Object instance) {
    return new MenuHooks(bind(onOpen, instance), bind(onClose, instance));
  }

  private static List<Consumer<Player>> bind(List<Handler> handlers, Object instance) {
    return handlers.stream().map(handler -> handler.bind(instance)).toList();
  }
}
