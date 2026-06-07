package dev.haniel.menu.paper.discovery;

import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.discovery.DiscoveredMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A {@link MenuDiscovery} backed by ClassGraph.
 *
 * <p>Scans the given base packages once at boot for {@code @Menu} classes. The {@link ScanResult}
 * is always closed (try-with-resources) — ClassGraph leaks native and thread resources otherwise.
 * Results are ordered by {@code MenuId} so registration does not depend on classpath order.
 */
public final class ClassGraphMenuDiscovery implements MenuDiscovery {

  @Override
  public List<DiscoveredMenu> discover(Set<String> basePackages) {
    try (ScanResult scan = scan(basePackages)) {
      return menus(scan);
    }
  }

  private ScanResult scan(Set<String> basePackages) {
    return new ClassGraph()
        .enableAnnotationInfo()
        .acceptPackages(basePackages.toArray(String[]::new))
        .scan();
  }

  private List<DiscoveredMenu> menus(ScanResult scan) {
    return scan.getClassesWithAnnotation(Menu.class).loadClasses().stream()
        .map(DiscoveredMenu::from)
        .sorted(Comparator.comparing(menu -> menu.id().value()))
        .toList();
  }
}
