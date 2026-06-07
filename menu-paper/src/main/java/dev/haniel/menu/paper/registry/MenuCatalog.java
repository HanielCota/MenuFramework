package dev.haniel.menu.paper.registry;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MenuId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A first-class collection of registered menus keyed by id.
 *
 * <p>Backed by a {@link ConcurrentHashMap} so {@link #reloadAllReportAsync} can iterate the catalog
 * on an async thread while registration happens on another, without a data race.
 */
public final class MenuCatalog {

  private final Map<MenuId, RegisteredMenu> menus = new ConcurrentHashMap<>();

  /**
   * Stores the menu under the given id, rejecting a duplicate id atomically.
   *
   * @param id the menu id; never null
   * @param menu the registered menu; never null
   * @throws InvalidMenuException if a menu is already registered under the id
   */
  public void put(MenuId id, RegisteredMenu menu) {
    if (menus.putIfAbsent(id, menu) != null) {
      throw new InvalidMenuException("Duplicate menu id '" + id.value() + "'");
    }
  }

  /**
   * Finds the menu registered under the given id.
   *
   * @param id the menu id
   * @return the registered menu, or empty if none is registered
   */
  public Optional<RegisteredMenu> find(MenuId id) {
    return Optional.ofNullable(menus.get(id));
  }

  /**
   * Finds the menu registered from the given source class.
   *
   * @param sourceType the annotated menu class; never null
   * @return the registered menu, or empty if none is registered
   */
  public Optional<RegisteredMenu> find(Class<?> sourceType) {
    return menus.values().stream().filter(menu -> menu.hasSourceType(sourceType)).findFirst();
  }

  /**
   * Returns every registered menu.
   *
   * @return an immutable snapshot of the registered menus
   */
  public Collection<RegisteredMenu> all() {
    return List.copyOf(menus.values());
  }

  /**
   * Returns how many menus are registered.
   *
   * @return the menu count
   */
  public int size() {
    return menus.size();
  }
}
