package dev.haniel.menu.item;

import java.util.Objects;
import java.util.UUID;

/**
 * The skin shown on a player-head {@link Icon}, decoupled from any Bukkit type.
 *
 * <p>Either an {@link Owner} (the head of a known player, by id) or a fixed {@link Texture} (a
 * base64 textures value, for custom heads). Carried in {@link ItemTraits} so it is part of the
 * icon's identity and the per-page render cache keys on it.
 */
public sealed interface HeadSkin {

  /**
   * The head of a player identified by uuid.
   *
   * <p>The skin is resolved from the server's profile cache when rendered, so a recently-seen
   * player shows their real skin; an unknown id shows the default head until the client resolves
   * it.
   *
   * @param uuid the player's unique id; never null
   */
  record Owner(UUID uuid) implements HeadSkin {

    public Owner {
      Objects.requireNonNull(uuid, "uuid");
    }
  }

  /**
   * A fixed custom head defined by its base64 textures value.
   *
   * <p>Fully local — no profile lookup — so the skin always renders, independent of any player.
   *
   * @param base64 the base64-encoded {@code textures} property value; never blank
   */
  record Texture(String base64) implements HeadSkin {

    public Texture {
      Objects.requireNonNull(base64, "base64");
      if (base64.isBlank()) {
        throw new IllegalArgumentException("Head texture cannot be blank");
      }
    }
  }
}
