package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import org.bukkit.entity.Player;

/**
 * The Paper-side {@link ClickContext}, carrying who clicked and how.
 *
 * @param player the clicking player's id
 * @param clickType the kind of click performed
 * @param playerEntity the clicking player on the owning region/thread
 */
public record PaperClickContext(PlayerId player, ClickType clickType, Player playerEntity)
    implements ClickContext {}
