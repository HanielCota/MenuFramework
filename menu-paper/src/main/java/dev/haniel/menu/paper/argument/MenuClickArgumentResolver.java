package dev.haniel.menu.paper.argument;

import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.paper.api.AnvilPromptOpener;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.paper.api.MenuOpener;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Supplies a {@link MenuClick} to {@code @Button} methods that ask for one. */
public final class MenuClickArgumentResolver implements ClickArgumentResolver {

  private final MiniMessage miniMessage;
  private final Navigation navigation;

  /**
   * Creates a resolver that builds clicks with the given serializer, opener and prompt opener.
   *
   * @param miniMessage the serializer the produced {@link MenuClick} sends with; never null
   * @param opener the opener backing {@link MenuClick#open}; never null
   * @param prompts the opener backing {@link MenuClick#prompt}; never null
   */
  public MenuClickArgumentResolver(
      MiniMessage miniMessage, MenuOpener opener, AnvilPromptOpener prompts) {
    this.miniMessage = miniMessage;
    this.navigation = new Navigation(opener, prompts);
  }

  @Override
  public boolean supports(Class<?> parameterType) {
    return parameterType == MenuClick.class;
  }

  @Override
  public Object resolve(ClickContext context) {
    return new MenuClick(
        PaperContexts.require(context), miniMessage, navigation.opener(), navigation.prompts());
  }

  /** The two openers a click is wired with, grouped to keep this resolver at two fields. */
  private record Navigation(MenuOpener opener, AnvilPromptOpener prompts) {}
}
