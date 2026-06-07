package dev.haniel.menu.compiler;

import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.compiler.reader.StaticReader;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.MenuLoader;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.merge.StaticMerger;

/**
 * Compiles a static menu: read bound behaviour, load appearance, merge.
 *
 * <p>Static menus keep a single boot-bound instance and a shared, pre-rendered template.
 *
 * @param <V> the platform visual type
 */
public final class StaticCompiler<V> {

  private final StaticReader reader;
  private final MenuLoader loader;
  private final StaticMerger<V> merger;

  /**
   * Wires the static compilation stages.
   *
   * @param reader reads bound behaviour from the instance; never null
   * @param loader loads appearance from YAML; never null
   * @param merger joins behaviour and appearance; never null
   */
  public StaticCompiler(StaticReader reader, MenuLoader loader, StaticMerger<V> merger) {
    this.reader = reader;
    this.loader = loader;
    this.merger = merger;
  }

  /**
   * Compiles the given annotated instance into a static menu.
   *
   * @param instance the menu instance; never null
   * @return the compiled static menu
   */
  public CompiledMenu<V> compile(Object instance) {
    MenuBlueprint blueprint = reader.read(instance);
    MenuConfig config = loader.load(blueprint.id());
    return merger.merge(blueprint, config);
  }

  /**
   * Compiles the given annotated instance with a preloaded configuration.
   *
   * @param instance the menu instance; never null
   * @param config the already loaded appearance; never null
   * @return the compiled static menu
   */
  public CompiledMenu<V> compile(Object instance, MenuConfig config) {
    MenuBlueprint blueprint = reader.read(instance);
    return merger.merge(blueprint, config);
  }

  /**
   * Loads one menu configuration.
   *
   * @param id the menu id; never null
   * @return the parsed configuration
   */
  public MenuConfig load(MenuId id) {
    return loader.load(id);
  }
}
