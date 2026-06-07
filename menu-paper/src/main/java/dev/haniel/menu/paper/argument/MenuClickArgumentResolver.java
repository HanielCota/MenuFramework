package dev.haniel.menu.paper.argument;

import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.paper.api.MenuClick;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Supplies a {@link MenuClick} to {@code @Button} methods that ask for one. */
public final class MenuClickArgumentResolver implements ClickArgumentResolver {

  private final MiniMessage miniMessage;

  /**
   * Creates a resolver that builds clicks with the given serializer.
   *
   * @param miniMessage the serializer the produced {@link MenuClick} sends with; never null
   */
  public MenuClickArgumentResolver(MiniMessage miniMessage) {
    this.miniMessage = miniMessage;
  }

  @Override
  public boolean supports(Class<?> parameterType) {
    return parameterType == MenuClick.class;
  }

  @Override
  public Object resolve(ClickContext context) {
    return new MenuClick(PaperContexts.require(context), miniMessage);
  }
}
