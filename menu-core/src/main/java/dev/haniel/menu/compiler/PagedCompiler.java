package dev.haniel.menu.compiler;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.compiler.reader.PagedReader;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.MenuLoader;
import dev.haniel.menu.merge.PagedMerger;
import java.util.Objects;

/**
 * Compiles a paginated menu: discover structure, load appearance, merge.
 *
 * <p>Paginated menus keep instance-free wiring and a fresh instance per open, so each player owns
 * independent reactive state.
 *
 * @param <V> the platform visual type
 */
public final class PagedCompiler<V> {

  private final PagedReader reader;
  private final MenuLoader loader;
  private final PagedMerger<V> merger;

  /**
   * Wires the paginated compilation stages.
   *
   * @param reader discovers structure from the class; never null
   * @param loader loads appearance from YAML; never null
   * @param merger joins structure and appearance; never null
   */
  public PagedCompiler(PagedReader reader, MenuLoader loader, PagedMerger<V> merger) {
    this.reader = Objects.requireNonNull(reader, "reader");
    this.loader = Objects.requireNonNull(loader, "loader");
    this.merger = Objects.requireNonNull(merger, "merger");
  }

  /**
   * Tells whether the given class is a paginated menu.
   *
   * @param type the menu class
   * @return {@code true} if it declares a {@code @Paginated} method
   */
  public boolean handles(Class<?> type) {
    return reader.handles(type);
  }

  /**
   * Compiles the given paginated menu class.
   *
   * @param type the menu class; never null
   * @return the compiled paginated menu
   */
  public CompiledMenu<V> compile(Class<?> type) {
    PagedStructure structure = reader.read(type);
    MenuConfig config = loader.load(structure.id());
    return merger.merge(structure, config);
  }

  /**
   * Compiles the given paginated menu class with a preloaded configuration.
   *
   * @param type the menu class; never null
   * @param config the already loaded appearance; never null
   * @return the compiled paginated menu
   */
  public CompiledMenu<V> compile(Class<?> type, MenuConfig config) {
    PagedStructure structure = reader.read(type);
    return merger.merge(structure, config);
  }

  /**
   * Compiles the given paginated menu class using the supplied per-open instance factory.
   *
   * @param type the menu class; never null
   * @param instantiator creates one fresh menu instance per open; never null
   * @return the compiled paginated menu
   */
  public CompiledMenu<V> compile(Class<?> type, Instantiator instantiator) {
    PagedStructure structure = reader.read(type, instantiator);
    MenuConfig config = loader.load(structure.id());
    return merger.merge(structure, config);
  }

  /**
   * Compiles the given paginated menu class using the supplied per-open instance factory and
   * preloaded configuration.
   *
   * @param type the menu class; never null
   * @param instantiator creates one fresh menu instance per open; never null
   * @param config the already loaded appearance; never null
   * @return the compiled paginated menu
   */
  public CompiledMenu<V> compile(Class<?> type, Instantiator instantiator, MenuConfig config) {
    PagedStructure structure = reader.read(type, instantiator);
    return merger.merge(structure, config);
  }
}
