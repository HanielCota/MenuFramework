package dev.haniel.menu.paper.argument;

import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;
import org.bukkit.entity.Player;

/**
 * Supplies the Bukkit {@link Player} to {@code @Button} methods that ask for one.
 *
 * <p>Honours the {@link ClickArgumentResolver} non-null contract: if the clicker disconnected
 * between the click and dispatch, it throws rather than injecting {@code null} into the handler.
 */
public final class PlayerArgumentResolver implements ClickArgumentResolver {

  @Override
  public boolean supports(Class<?> parameterType) {
    return parameterType == Player.class;
  }

  @Override
  public Object resolve(ClickContext context) {
    Player player = PaperContexts.require(context).playerEntity();
    if (player == null) {
      throw new IllegalStateException("Clicking player is no longer online");
    }
    return player;
  }
}
