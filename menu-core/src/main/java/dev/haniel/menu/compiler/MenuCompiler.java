package dev.haniel.menu.compiler;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.MenuId;
import java.util.Objects;
import java.util.function.Function;

/**
 * Orchestrates compilation, dispatching to the static or paginated path by structure.
 *
 * <p>Reflection lives in the readers and IO in the loader; both run only at boot or reload. A menu
 * declaring a {@code @Paginated} method compiles to the paginated path; otherwise the static one.
 *
 * @param <V> the platform visual type
 */
public final class MenuCompiler<V> {

  private final StaticCompiler<V> staticCompiler;
  private final PagedCompiler<V> pagedCompiler;

  /**
   * Wires the two compilation paths.
   *
   * @param staticCompiler the static path; never null
   * @param pagedCompiler the paginated path; never null
   */
  public MenuCompiler(StaticCompiler<V> staticCompiler, PagedCompiler<V> pagedCompiler) {
    this.staticCompiler = Objects.requireNonNull(staticCompiler, "staticCompiler");
    this.pagedCompiler = Objects.requireNonNull(pagedCompiler, "pagedCompiler");
  }

  /**
   * Compiles the given annotated prototype into a ready-to-open menu.
   *
   * @param prototype an object whose class is annotated with {@code @Menu}; never null
   * @return the compiled menu
   * @throws InvalidMenuException if the class or its configuration is invalid
   */
  public CompiledMenu<V> compile(Object prototype) {
    Class<?> type = prototype.getClass();
    if (pagedCompiler.handles(type)) {
      return pagedCompiler.compile(type);
    }
    return staticCompiler.compile(prototype);
  }

  /**
   * Compiles the given annotated type using the supplied instantiation strategy.
   *
   * @param type the annotated menu class; never null
   * @param instances creates menu instances for the class; never null
   * @return the compiled menu
   */
  public CompiledMenu<V> compile(Class<?> type, Function<Class<?>, Object> instances) {
    if (pagedCompiler.handles(type)) {
      return pagedCompiler.compile(type, new Instantiator(() -> instances.apply(type)));
    }
    return staticCompiler.compile(instances.apply(type));
  }

  /**
   * Compiles the given annotated prototype with a preloaded configuration.
   *
   * @param prototype an object whose class is annotated with {@code @Menu}; never null
   * @param config the already loaded appearance; never null
   * @return the compiled menu
   */
  public CompiledMenu<V> compile(Object prototype, MenuConfig config) {
    Class<?> type = prototype.getClass();
    if (pagedCompiler.handles(type)) {
      return pagedCompiler.compile(type, config);
    }
    return staticCompiler.compile(prototype, config);
  }

  /**
   * Compiles the given annotated type using the supplied instantiation strategy and preloaded
   * configuration.
   *
   * @param type the annotated menu class; never null
   * @param instances creates menu instances for the class; never null
   * @param config the already loaded appearance; never null
   * @return the compiled menu
   */
  public CompiledMenu<V> compile(
      Class<?> type, Function<Class<?>, Object> instances, MenuConfig config) {
    if (pagedCompiler.handles(type)) {
      return pagedCompiler.compile(type, new Instantiator(() -> instances.apply(type)), config);
    }
    return staticCompiler.compile(instances.apply(type), config);
  }

  /**
   * Loads one menu configuration.
   *
   * @param id the menu id; never null
   * @return the parsed configuration
   */
  public MenuConfig load(MenuId id) {
    return staticCompiler.load(id);
  }
}
