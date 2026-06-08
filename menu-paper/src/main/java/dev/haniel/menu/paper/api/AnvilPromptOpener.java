package dev.haniel.menu.paper.api;

import org.bukkit.entity.Player;

/**
 * Opens an {@link AnvilPrompt} for a player.
 *
 * <p>The framework's anvil-input entry point, exposed to {@link MenuClick#prompt} so a button can
 * ask for text without wiring its own anvil listener. Implementations open the anvil on the calling
 * thread (the main server thread on Paper), so call it from a button action.
 */
public interface AnvilPromptOpener {

  /**
   * Opens the given prompt's anvil for the player.
   *
   * @param viewer the player to prompt; never null
   * @param prompt the prompt to open; never null
   */
  void open(Player viewer, AnvilPrompt<?> prompt);
}
