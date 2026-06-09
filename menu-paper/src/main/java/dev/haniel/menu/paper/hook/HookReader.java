package dev.haniel.menu.paper.hook;

import dev.haniel.menu.annotation.OnClose;
import dev.haniel.menu.annotation.OnOpen;
import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * Resolves the {@code @OnOpen}/{@code @OnClose} handlers of a menu class via reflection.
 *
 * <p>This reflection lives in the Paper layer rather than the core readers because a hook may
 * accept a Bukkit {@code Player}, a platform type the domain must not see. Runs once per class; the
 * result is cached by {@link HookDefinitions#of(Class)}.
 */
final class HookReader {

  private HookReader() {}

  static HookDefinitions read(Class<?> type) {
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
}
