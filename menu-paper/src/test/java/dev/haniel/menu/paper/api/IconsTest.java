package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.haniel.menu.item.HeadSkin;
import dev.haniel.menu.item.Icon;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

/** The head-icon factories build {@code PLAYER_HEAD} icons carrying the right skin. */
class IconsTest {

  @Test
  void headByIdBuildsAPlayerHeadOwnedByThatId() {
    UUID id = UUID.randomUUID();

    Icon icon = Icons.head(id);

    assertEquals("PLAYER_HEAD", icon.material());
    assertEquals(Optional.of(new HeadSkin.Owner(id)), icon.traits().head());
  }

  @Test
  void headByOfflinePlayerUsesItsId() {
    UUID id = UUID.randomUUID();
    OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.getUniqueId()).thenReturn(id);

    assertEquals(Optional.of(new HeadSkin.Owner(id)), Icons.head(player).traits().head());
  }

  @Test
  void headTextureCarriesTheBase64() {
    Icon icon = Icons.headTexture("dGV4dHVyZQ==");

    assertEquals("PLAYER_HEAD", icon.material());
    assertEquals(Optional.of(new HeadSkin.Texture("dGV4dHVyZQ==")), icon.traits().head());
  }

  /** Two different heads must be unequal icons, or the per-page render cache would collide. */
  @Test
  void differentHeadsAreDistinctIcons() {
    assertNotEquals(Icons.head(UUID.randomUUID()), Icons.head(UUID.randomUUID()));
    assertNotEquals(Icons.headTexture("aaa"), Icons.headTexture("bbb"));
  }
}
