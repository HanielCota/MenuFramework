package dev.haniel.menu.discovery;

import java.util.List;
import java.util.Set;

/**
 * Discovers {@code @Menu} classes under a set of base packages.
 *
 * <p>The contract lives in the core; the scanning implementation (ClassGraph) lives in the platform
 * layer, so the core carries no scanning dependency. Implementations run once at boot, never at
 * runtime, and must return a deterministic, classpath-order-independent ordering.
 */
public interface MenuDiscovery {

  /**
   * Finds every {@code @Menu} class under the given base packages.
   *
   * @param basePackages the packages to scan; never null
   * @return the discovered menus, ordered deterministically by id
   */
  List<DiscoveredMenu> discover(Set<String> basePackages);
}
