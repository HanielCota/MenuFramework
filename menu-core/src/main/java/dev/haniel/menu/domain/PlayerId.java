package dev.haniel.menu.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * The stable identifier of a player, decoupled from any Bukkit type.
 *
 * @param value the player's unique id; never null
 */
public record PlayerId(UUID value) {

  public PlayerId {
    Objects.requireNonNull(value, "value");
  }
}
