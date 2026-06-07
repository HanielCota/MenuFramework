package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerIdTest {

  @Test
  void keepsTheGivenUuid() {
    UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

    assertEquals(uuid, new PlayerId(uuid).value());
  }

  @Test
  void rejectsNullUuid() {
    assertThrows(NullPointerException.class, () -> new PlayerId(null));
  }
}
