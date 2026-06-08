package dev.haniel.menu.discovery;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MenuId;

/**
 * A discovered {@code @Menu} class paired with its resolved id.
 *
 * <p>Holds the class, never an instance; the existing pipeline creates and compiles it later. The
 * id enables deterministic, classpath-order-independent registration.
 *
 * @param type the discovered menu class
 * @param id the id read from its {@code @Menu} annotation
 */
public record DiscoveredMenu(Class<?> type, MenuId id) {

  /**
   * Builds a discovered menu from a class, validating that it is annotated with {@code @Menu}.
   *
   * @param type a class expected to carry {@code @Menu}; never null
   * @return the discovered menu with its resolved id
   * @throws InvalidMenuException if the class is not annotated with {@code @Menu}
   */
  public static DiscoveredMenu from(Class<?> type) {
    Menu menu = type.getAnnotation(Menu.class);
    if (menu == null) {
      throw new InvalidMenuException(type.getName() + " is not annotated with @Menu");
    }
    try {
      return new DiscoveredMenu(type, new MenuId(menu.id()));
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "@Menu id on " + type.getName() + " is invalid: " + exception.getMessage(), exception);
    }
  }
}
