package dev.haniel.menu.placeholder;

import dev.haniel.menu.domain.PlayerId;

/**
 * Resolves placeholder tokens in a string for a specific viewer, before MiniMessage parsing.
 *
 * <p>The platform-neutral seam for per-player text such as {@code %player_name%}. The domain only
 * depends on this contract; the Paper layer supplies a PlaceholderAPI-backed implementation,
 * falling back to {@link #none()} when the plugin is absent.
 */
@FunctionalInterface
public interface PlaceholderResolver {

  /**
   * Resolves the placeholders in the given text for the given player.
   *
   * @param player the viewer the text is rendered for; never null
   * @param text the raw text, possibly containing placeholders; never null
   * @return the text with placeholders replaced, or the input when there is nothing to replace
   */
  String resolve(PlayerId player, String text);

  /**
   * Returns a resolver that returns text unchanged.
   *
   * @return the identity resolver
   */
  static PlaceholderResolver none() {
    return (player, text) -> text;
  }
}
