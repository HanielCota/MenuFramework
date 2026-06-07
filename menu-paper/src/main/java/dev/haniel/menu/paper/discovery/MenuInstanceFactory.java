package dev.haniel.menu.paper.discovery;

import java.util.function.Function;

/**
 * Creates annotated menu instances for discovered classes.
 *
 * <p>Implementations may resolve constructor dependencies from a container. The framework calls
 * this at registration/reload time for static menus and keeps it as the per-open factory for
 * paginated menus.
 */
@FunctionalInterface
public interface MenuInstanceFactory extends Function<Class<?>, Object> {

  /**
   * Creates one menu instance for the given class.
   *
   * @param menuType the annotated menu class; never null
   * @return a menu instance; never null
   */
  Object create(Class<?> menuType);

  @Override
  default Object apply(Class<?> menuType) {
    return create(menuType);
  }
}
