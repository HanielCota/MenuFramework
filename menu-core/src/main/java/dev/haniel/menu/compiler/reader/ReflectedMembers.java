package dev.haniel.menu.compiler.reader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Walks a class hierarchy, excluding {@link Object}, collecting its declared members.
 *
 * <p>The single home of the reflection-by-inheritance walk shared by the static and paginated
 * readers, so a menu's {@code @Button}/{@code @Reactive} members are discovered the same way down
 * to inherited base classes.
 */
final class ReflectedMembers {

  private ReflectedMembers() {}

  /**
   * Returns every declared method on the type and its superclasses, up to but excluding {@link
   * Object}.
   *
   * @param type the class to scan; never null
   * @return the declared methods in subclass-first order
   */
  static List<Method> methods(Class<?> type) {
    List<Method> methods = new ArrayList<>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      methods.addAll(Arrays.asList(current.getDeclaredMethods()));
      current = current.getSuperclass();
    }
    return methods;
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
