package dev.haniel.menu.paper.discovery;

import dev.haniel.menu.discovery.DiscoveredMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Orchestrates boot discovery: discover {@code @Menu} classes, instantiate each, hand it to a
 * registrar, and aggregate every failure into one clear boot error.
 *
 * <p>Decoupled from the registry through the registrar callback, so the discover/instantiate/
 * aggregate flow is testable without a server.
 */
public final class MenuScanner {

  private final MenuDiscovery discovery;
  private final Function<Class<?>, Object> instances;

  /**
   * Wires discovery to instantiation.
   *
   * @param discovery the {@code @Menu} discovery; never null
   * @param instances the instantiation strategy; never null
   */
  public MenuScanner(MenuDiscovery discovery, Function<Class<?>, Object> instances) {
    this.discovery = discovery;
    this.instances = instances;
  }

  /**
   * Discovers and registers every menu under the base packages, failing the boot if any fail.
   *
   * @param basePackages the packages to scan; never null
   * @param registrar receives each successfully created instance; never null
   * @throws MenuDiscoveryException if one or more menus fail
   */
  public void scan(Set<String> basePackages, Consumer<Object> registrar) {
    MenuErrors errors = new MenuErrors();
    discovery.discover(basePackages).forEach(menu -> register(menu, registrar, errors));
    errors.failIfAny();
  }

  /**
   * Discovers and registers every menu class under the base packages.
   *
   * @param basePackages the packages to scan; never null
   * @param registrar receives each discovered type; never null
   * @throws MenuDiscoveryException if one or more menus fail
   */
  public void scanTypes(Set<String> basePackages, Consumer<Class<?>> registrar) {
    MenuErrors errors = new MenuErrors();
    discovery.discover(basePackages).forEach(menu -> registerType(menu, registrar, errors));
    errors.failIfAny();
  }

  private void register(DiscoveredMenu menu, Consumer<Object> registrar, MenuErrors errors) {
    try {
      registrar.accept(instances.apply(menu.type()));
    } catch (RuntimeException failure) {
      errors.add(menu.type(), failure);
    }
  }

  private void registerType(DiscoveredMenu menu, Consumer<Class<?>> registrar, MenuErrors errors) {
    try {
      registrar.accept(menu.type());
    } catch (RuntimeException failure) {
      errors.add(menu.type(), failure);
    }
  }
}
