package dev.haniel.menu.paper.api;

import dev.haniel.menu.item.HeadSkin;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.ItemTraits;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

/**
 * Type-safe entry point for building {@link Icon}s in code from a Bukkit {@link Material}.
 *
 * <p>Prefer this over {@link Icon#of(String)} when writing menu code: a {@link Material} is checked
 * by the compiler, so a typo cannot slip through to a runtime render failure. The returned {@link
 * Icon} is the same immutable value object, so {@code named} and {@code describedBy} chain as
 * usual.
 */
public final class Icons {

  private Icons() {}

  /**
   * Creates an icon for the given material with no name or lore.
   *
   * @param material the Bukkit material; never null
   * @return a new icon backed by {@code material.name()}
   */
  public static Icon of(Material material) {
    return Icon.of(material.name());
  }

  /**
   * Creates a player-head icon showing the given player's skin.
   *
   * @param player the player whose head to show; never null
   * @return a new {@code PLAYER_HEAD} icon, skinned from the player's id
   */
  public static Icon head(OfflinePlayer player) {
    return head(player.getUniqueId());
  }

  /**
   * Creates a player-head icon showing the head of the player with the given id.
   *
   * <p>The skin is resolved from the server's profile cache when rendered; an unknown id shows the
   * default head until the client resolves it. For a guaranteed custom skin use {@link
   * #headTexture(String)}.
   *
   * @param owner the player's unique id; never null
   * @return a new {@code PLAYER_HEAD} icon, skinned from the id
   */
  public static Icon head(UUID owner) {
    return headOf(new HeadSkin.Owner(owner));
  }

  /**
   * Creates a player-head icon with a fixed custom skin from a base64 textures value.
   *
   * @param base64 the base64-encoded {@code textures} property value; never blank
   * @return a new {@code PLAYER_HEAD} icon with the given texture
   */
  public static Icon headTexture(String base64) {
    return headOf(new HeadSkin.Texture(base64));
  }

  private static Icon headOf(HeadSkin skin) {
    return new Icon(Material.PLAYER_HEAD.name(), "", List.of(), ItemTraits.none().withHead(skin));
  }
}
