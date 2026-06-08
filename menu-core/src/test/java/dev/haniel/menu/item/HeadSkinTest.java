package dev.haniel.menu.item;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Validation and identity of the player-head skin value object. */
class HeadSkinTest {

  @Test
  void ownerRejectsNullId() {
    assertThrows(NullPointerException.class, () -> new HeadSkin.Owner(null));
  }

  @Test
  void textureRejectsNull() {
    assertThrows(NullPointerException.class, () -> new HeadSkin.Texture(null));
  }

  @Test
  void textureRejectsBlank() {
    assertThrows(IllegalArgumentException.class, () -> new HeadSkin.Texture("  "));
  }

  @Test
  void ownersWithDifferentIdsDiffer() {
    assertNotEquals(new HeadSkin.Owner(UUID.randomUUID()), new HeadSkin.Owner(UUID.randomUUID()));
  }
}
