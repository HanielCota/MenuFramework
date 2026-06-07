package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import org.bukkit.entity.Player;

/**
 * The Paper-side {@link ClickContext}, carrying who clicked and how.
 *
 * <p>Does not store the live {@link Player} reference to avoid memory leaks; the entity is resolved
 * on demand via {@link #playerEntity()}.
 *
 * @param player the clicking player's id
 * @param clickType the kind of click performed
 */
public record PaperClickContext(PlayerId player, ClickType clickType) implements ClickContext {

  /**
   * Resolves the clicking player from the server. May return {@code null} if the player has
   * disconnected since the click occurred.
   *
   * @return the clicking player, or {@code null} if offline
   */
  public Player playerEntity() {
    return org.bukkit.Bukkit.getPlayer(player.value());
  }

  @Override
  public boolean hasPermission(String permission) {
    Player online = playerEntity();
    return online != null && online.hasPermission(permission);
  }
}
