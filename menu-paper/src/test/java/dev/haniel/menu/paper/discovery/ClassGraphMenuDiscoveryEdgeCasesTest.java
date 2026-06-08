package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.discovery.MenuDiscovery;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Adversarial edge cases for {@link ClassGraphMenuDiscovery}: empty/unknown packages. */
class ClassGraphMenuDiscoveryEdgeCasesTest {

  private final MenuDiscovery discovery = new ClassGraphMenuDiscovery();

  /** Scanning a package with no @Menu classes must yield an empty list, never null or error. */
  @Test
  void unknownPackageYieldsEmptyList() {
    assertTrue(discovery.discover(Set.of("dev.haniel.menu.paper.nonexistent.pkg")).isEmpty());
  }

  /** Scanning an empty package set must not blow up and must return nothing useful. */
  @Test
  void emptyPackageSetIsHandledGracefully() {
    assertDoesNotThrow(() -> discovery.discover(Set.of()));
  }

  /** A package containing both @Menu and plain classes must discover only the annotated ones. */
  @Test
  void discoversOnlyAnnotatedClassesIgnoringPlainOnes() {
    int count = discovery.discover(Set.of("dev.haniel.menu.paper.samples")).size();

    assertEquals(2, count, "PlainClass must be ignored; only @Menu classes discovered");
  }

  @Test
  void aggregatesInvalidDiscoveredMenuIds() {
    MenuDiscoveryException failure =
        assertThrows(
            MenuDiscoveryException.class,
            () -> discovery.discover(Set.of("dev.haniel.menu.paper.invalidsamples")));

    assertTrue(failure.getMessage().contains("MenuInvalidId"));
    assertTrue(failure.getMessage().contains("@Menu id"));
  }
}
