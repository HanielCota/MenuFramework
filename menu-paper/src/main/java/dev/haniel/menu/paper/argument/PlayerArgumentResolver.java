package dev.haniel.menu.paper.argument;

import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;
import org.bukkit.entity.Player;

/** Supplies the Bukkit {@link Player} to {@code @Button} methods that ask for one. */
public final class PlayerArgumentResolver implements ClickArgumentResolver {

  @Override
  public boolean supports(Class<?> parameterType) {
    return parameterType == Player.class;
  }

  @Override
  public Object resolve(ClickContext context) {
    return PaperContexts.require(context).playerEntity();
  }
}
