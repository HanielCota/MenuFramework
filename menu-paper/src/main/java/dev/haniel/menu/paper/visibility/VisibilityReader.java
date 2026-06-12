package dev.haniel.menu.paper.visibility;

import dev.haniel.menu.annotation.Visible;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.paper.visibility.VisibilityRules.Rule;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 * Resolves the {@code @Visible} rules of a menu class via reflection.
 *
 * <p>Lives in the Paper layer, not the core readers, because a rule may accept a Bukkit {@code
 * Player}. Runs once per class; the result is cached by {@link VisibilityRules#of(Class)}.
 */
final class VisibilityReader {

  private VisibilityReader() {}

  static VisibilityRules read(Class<?> type) {
    Map<String, Rule> rules = new HashMap<>();
    for (Method method : methods(type)) {
      collect(method, rules);
    }
    return new VisibilityRules(Map.copyOf(rules));
  }

  private static void collect(Method method, Map<String, Rule> rules) {
    Visible visible = method.getAnnotation(Visible.class);
    if (visible == null) {
      return;
    }
    if (rules.putIfAbsent(visible.value(), rule(method)) != null) {
      throw new InvalidMenuException(
          "Duplicate @Visible rule for button '" + visible.value() + "'");
    }
  }

  @SuppressWarnings("java:S3011") // Visibility rules may be private annotated methods.
  private static Rule rule(Method method) {
    boolean acceptsPlayer = validate(method);
    try {
      method.setAccessible(true);
      return new Rule(MethodHandles.lookup().unreflect(method), acceptsPlayer);
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException(
          "Cannot access @Visible method " + method.getName(), exception);
    }
  }

  private static boolean validate(Method method) {
    boolean returnsBoolean = method.getReturnType() == boolean.class;
    boolean playerParam =
        method.getParameterCount() == 1 && method.getParameterTypes()[0] == Player.class;
    boolean validShape = returnsBoolean && method.getParameterCount() <= 1;
    if (!validShape || (method.getParameterCount() == 1 && !playerParam)) {
      throw new InvalidMenuException(
          "@Visible method "
              + method.getName()
              + " must return boolean and take no args or a single Player");
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
