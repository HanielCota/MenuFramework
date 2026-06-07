package dev.haniel.menu.paper.render.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DataVersionTest {

  @Test
  void startsAtZero() {
    assertEquals(0L, new DataVersion().current());
  }

  @Test
  void bumpAdvancesByOne() {
    DataVersion version = new DataVersion();

    version.bump();

    assertEquals(1L, version.current());
  }

  @Test
  void bumpIsMonotonicAcrossManyCalls() {
    DataVersion version = new DataVersion();

    version.bump();
    version.bump();
    version.bump();

    assertEquals(3L, version.current());
  }

  @Test
  void currentDoesNotAdvanceOnItsOwn() {
    DataVersion version = new DataVersion();

    version.current();
    version.current();

    assertEquals(0L, version.current());
  }
}
