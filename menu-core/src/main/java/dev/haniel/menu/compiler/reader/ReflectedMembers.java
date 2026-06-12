package dev.haniel.menu.compiler.reader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Walks a class hierarchy, excluding {@link Object}, collecting its declared members.
 *
 * <p>The single home of the reflection-by-inheritance walk shared by the static and paginated
 * readers, so a menu's {@code @Button}/{@code @Reactive} members are discovered the same way down
 * to inherited base classes. Compiler-generated bridge and synthetic methods are skipped (javac
 * copies annotations onto bridges, which would surface as phantom duplicates), and a method
 * overridden by a subclass is reported once, as the override.
 */
final class ReflectedMembers {

  private ReflectedMembers() {}

  /**
   * Returns every declared method on the type and its superclasses, up to but excluding {@link
   * Object}.
   *
   * @param type the class to scan; never null
   * @return the declared methods in subclass-first order, overrides deduplicated
   */
  static List<Method> methods(Class<?> type) {
    List<Method> methods = new ArrayList<>();
    Set<String> overridden = new HashSet<>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      collectDeclared(current, methods, overridden);
      current = current.getSuperclass();
    }
    return methods;
  }

  private static void collectDeclared(Class<?> type, List<Method> methods, Set<String> overridden) {
    Arrays.stream(type.getDeclaredMethods())
        .filter(method -> !method.isBridge() && !method.isSynthetic())
        .filter(method -> isDistinct(method, overridden))
        .forEach(methods::add);
  }

  // Private methods never participate in overriding: a same-signature private method on a
  // superclass is a distinct member, so only non-private declarations shadow ancestors.
  private static boolean isDistinct(Method method, Set<String> overridden) {
    if (Modifier.isPrivate(method.getModifiers())) {
      return true;
    }
    return overridden.add(method.getName() + Arrays.toString(method.getParameterTypes()));
  }

  /**
   * Returns every declared field on the type and its superclasses, up to but excluding {@link
   * Object}.
   *
   * @param type the class to scan; never null
   * @return the declared fields in subclass-first order
   */
  static List<Field> fields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      fields.addAll(Arrays.asList(current.getDeclaredFields()));
      current = current.getSuperclass();
    }
    return fields;
  }
}
