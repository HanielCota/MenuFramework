package dev.haniel.menu.paper.hook;

import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.annotation.OnClose;
import dev.haniel.menu.annotation.OnOpen;
import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 * The {@code @OnOpen}/{@code @OnClose} handlers of a menu class, resolved once and cached.
 *
 * <p>This reflection lives in the Paper layer rather than the core readers because a hook may
 * accept a Bukkit {@code Player}, a platform type the domain must not see. Handlers are read once
 * per class (cached) and bound to a fresh instance per open by {@link #bind(Object)}.
 */
public final class HookDefinitions {

  private static final ConcurrentMap<Class<?>, HookDefinitions> CACHE = new ConcurrentHashMap<>();

  private final List<Handler> onOpen;
  private final List<Handler> onClose;

  private HookDefinitions(List<Handler> onOpen, List<Handler> onClose) {
    this.onOpen = onOpen;
    this.onClose = onClose;
  }

  /**
   * Returns the cached handlers for the given menu class, reading them on first use.
   *
   * @param type the menu class; never null
   * @return the resolved handlers
   * @throws InvalidMenuException if a handler has an unsupported signature
   */
  public static HookDefinitions of(Class<?> type) {
    return CACHE.computeIfAbsent(type, HookDefinitions::read);
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

  private static HookDefinitions read(Class<?> type) {
    List<Handler> open = new ArrayList<>();
    List<Handler> close = new ArrayList<>();
    for (Method method : methods(type)) {
      collect(method, OnOpen.class, open);
      collect(method, OnClose.class, close);
    }
    return new HookDefinitions(List.copyOf(open), List.copyOf(close));
  }

  private static void collect(
      Method method, Class<? extends Annotation> annotation, List<Handler> into) {
    if (method.isAnnotationPresent(annotation)) {
      into.add(handler(method, annotation));
    }
  }

  @SuppressWarnings("java:S3011") // Lifecycle handlers may be private annotated methods.
  private static Handler handler(Method method, Class<? extends Annotation> annotation) {
    boolean acceptsPlayer = validate(method, annotation);
    try {
      method.setAccessible(true);
      return new Handler(MethodHandles.lookup().unreflect(method), acceptsPlayer);
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException(
          "Cannot access @" + annotation.getSimpleName() + " method", exception);
    }
  }

  private static boolean validate(Method method, Class<? extends Annotation> annotation) {
    boolean validShape = method.getReturnType() == void.class && method.getParameterCount() <= 1;
    boolean playerParam =
        method.getParameterCount() == 1 && method.getParameterTypes()[0] == Player.class;
    if (!validShape || (method.getParameterCount() == 1 && !playerParam)) {
      throw new InvalidMenuException(
          "@"
              + annotation.getSimpleName()
              + " method "
              + method.getName()
              + " must return void and take no args or a single Player");
    }
    return playerParam;
  }

  private static List<Method> methods(Class<?> type) {
    List<Method> methods = new ArrayList<>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      methods.addAll(List.of(current.getDeclaredMethods()));
      current = current.getSuperclass();
    }
    return methods;
  }

  private record Handler(MethodHandle handle, boolean acceptsPlayer) {

    Consumer<Player> bind(Object instance) {
      MethodHandle bound = handle.bindTo(instance);
      return acceptsPlayer ? player -> invokeWithPlayer(bound, player) : player -> invoke(bound);
    }

    // A player-accepting handler cannot run for an absent viewer (a close fired after disconnect),
    // so it is skipped; a no-arg handler still runs and can perform viewer-independent cleanup.
    private static void invokeWithPlayer(MethodHandle bound, Player player) {
      if (player != null) {
        invoke(bound, player);
      }
    }

    @SuppressWarnings(
        "java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
    private static void invoke(MethodHandle bound, Object... args) {
      try {
        bound.invokeWithArguments(args);
      } catch (Error error) {
        throw error;
      } catch (Throwable throwable) {
        throw new MenuActionException("Lifecycle hook failed", throwable);
      }
    }
  }
}
