package dev.haniel.menu.paper.api;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.listener.PaperClickContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * The click handed to a {@code @Button} method that asks for it.
 *
 * <p>Wraps who clicked and how, plus the few things a menu button usually does: read the player,
 * send a MiniMessage line, or close the menu. Prefer asking for this over a raw {@link
 * ClickContext} and casting: a button method may simply declare {@code void onClick(MenuClick
 * click)}.
 */
public final class MenuClick {

  private static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

  private final PaperClickContext context;
  private final MiniMessage miniMessage;

  /**
   * Creates a click over the given Paper context.
   *
   * @param context the Paper-side click context; never null
   * @param miniMessage the serializer used by {@link #message(String)}; never null
   */
  public MenuClick(PaperClickContext context, MiniMessage miniMessage) {
    this.context = context;
    this.miniMessage = miniMessage;
  }

  /**
   * Wraps a click context for code-built {@link dev.haniel.menu.item.MenuItem} actions.
   *
   * @param context the click context received by a {@code MenuItem} action; never null
   * @return a click using the default MiniMessage serializer
   * @throws IllegalStateException if the context is not a Paper click
   */
  public static MenuClick of(ClickContext context) {
    if (!(context instanceof PaperClickContext paper)) {
      throw new IllegalStateException("MenuClick is only available on Paper");
    }
    return new MenuClick(paper, DEFAULT_MINI_MESSAGE);
  }

  /**
   * Returns the player who clicked.
   *
   * @return the clicking player; never null
   */
  public Player player() {
    return context.playerEntity();
  }

  /**
   * Returns the clicking player's stable id.
   *
   * @return the player id; never null
   */
  public PlayerId playerId() {
    return context.player();
  }

  /**
   * Returns the kind of click performed.
   *
   * @return the click type; never null
   */
  public ClickType clickType() {
    return context.clickType();
  }

  /**
   * Sends a MiniMessage line to the player who clicked.
   *
   * <p><strong>Security:</strong> the text is parsed as <em>trusted</em> MiniMessage with every tag
   * enabled, including {@code <click:run_command>} and {@code <hover>}. Never pass unescaped
   * player-controlled text (names, chat, sign or anvil input) — escape it with {@link
   * MiniMessage#escapeTags(String)} or insert it through a placeholder/{@code Component} argument
   * first, or a player could inject arbitrary click and hover actions.
   *
   * @param miniMessageText the trusted MiniMessage string to deserialize and send; never null
   */
  public void message(String miniMessageText) {
    player().sendMessage(miniMessage.deserialize(miniMessageText));
  }

  /** Closes the menu for the player who clicked. */
  public void close() {
    player().closeInventory();
  }
}
