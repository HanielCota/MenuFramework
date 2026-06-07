package dev.haniel.menu.paper.discovery;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.reflect.Constructor;

/**
 * Creates a menu instance from a discovered class using its no-arg constructor.
 *
 * <p>The chosen instantiation strategy: every {@code @Menu} class must have an accessible no-arg
 * constructor (the strategy the manual {@code register(new HelloMenu())} already relied on). A
 * missing or failing constructor is a clear boot error naming the class — never a silent NPE.
 */
public final class MenuInstantiator implements MenuInstanceFactory {

  /**
   * Instantiates the given menu class.
   *
   * @param type the discovered menu class; never null
   * @return a new instance
   * @throws InvalidMenuException if the class has no usable no-arg constructor
   */
  @SuppressWarnings("java:S3011") // Discovered menu classes may keep constructors private.
  public Object create(Class<?> type) {
    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException exception) {
      throw new InvalidMenuException(
          type.getName() + " needs an accessible no-arg constructor", exception);
    }
  }
}
