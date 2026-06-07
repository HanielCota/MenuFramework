package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.discovery.DiscoveredMenu;
import dev.haniel.menu.discovery.MenuDiscovery;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClassGraphMenuDiscoveryTest {

  private static final Set<String> SAMPLES = Set.of("dev.haniel.menu.paper.samples");

  private final MenuDiscovery discovery = new ClassGraphMenuDiscovery();

  @Test
  void findsExactlyTheAnnotatedClassesSortedById() {
    List<String> ids = discovery.discover(SAMPLES).stream().map(menu -> menu.id().value()).toList();
    assertEquals(List.of("alpha", "bravo"), ids);
  }

  @Test
  void isDeterministicAcrossScans() {
    List<DiscoveredMenu> first = discovery.discover(SAMPLES);
    List<DiscoveredMenu> second = discovery.discover(SAMPLES);
    assertEquals(first, second);
  }
}
