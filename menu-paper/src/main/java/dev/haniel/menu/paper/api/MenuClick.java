package dev.haniel.menu.paper.api;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * The click handed to a {@code @Button} method that asks for it.
 *
 * <p>Wraps who clicked and how, plus the few things a menu button usually does: read the player,
 * send a MiniMessage line, open another menu, or close this one. Prefer asking for this over a raw
 * {@link ClickContext} and casting: a button method may simply declare {@code void
 * onClick(MenuClick click)}.
 */
public final class MenuClick {

  private static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

  private final PaperClickContext context;
  private final ClickServices services;

  /**
   * Creates a click over the given Paper context, without navigation or prompts.
   *
   * @param context the Paper-side click context; never null
   * @param miniMessage the serializer used by {@link #message(String)}; never null
   */
  public MenuClick(PaperClickContext context, MiniMessage miniMessage) {
    this(context, miniMessage, null, null);
  }

  /**
   * Creates a click over the given Paper context with navigation and prompts.
   *
   * @param context the Paper-side click context; never null
   * @param miniMessage the serializer used by {@link #message(String)}; never null
   * @param opener the opener backing {@link #open}; {@code null} disables navigation
   * @param prompts the opener backing {@link #prompt}; {@code null} disables prompts
   */
  public MenuClick(
      PaperClickContext context,
      MiniMessage miniMessage,
      MenuOpener opener,
      AnvilPromptOpener prompts) {
    this.context = Objects.requireNonNull(context, "context");
    this.services =
        new ClickServices(Objects.requireNonNull(miniMessage, "miniMessage"), opener, prompts);
  }

  /**
   * Wraps a click context for code-built {@link dev.haniel.menu.item.MenuItem} actions.
   *
   * <p>The returned click can read the player, send messages and close; {@link #open} and {@link
   * #prompt} are not available on this path. Navigate or prompt from a {@code @Button} method that
   * declares a {@code MenuClick} parameter instead.
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
    Player online = context.playerEntity();
    if (online == null) {
      throw new IllegalStateException("Player is no longer online");
    }
    return online;
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
    player().sendMessage(services.render(miniMessageText));
  }

  /**
   * Opens the registered menu with the given id for the player who clicked.
   *
   * @param id the menu id to open; never null
   * @throws IllegalStateException if this click was not built with navigation (see {@link
   *     #of(ClickContext)})
   */
  public void open(MenuId id) {
    services.requireOpener().open(player(), id);
  }

  /**
   * Opens the registered menu for the given class for the player who clicked.
   *
   * @param menuType the registered menu class; never null
   * @throws IllegalStateException if this click was not built with navigation (see {@link
   *     #of(ClickContext)})
   */
  public void open(Class<?> menuType) {
    services.requireOpener().open(player(), menuType);
  }

  /**
   * Opens an anvil text prompt for the player who clicked.
   *
   * @param prompt the prompt to open; never null
   * @throws IllegalStateException if this click was not built with prompts (see {@link
   *     #of(ClickContext)})
   */
  public void prompt(AnvilPrompt<?> prompt) {
    services.requirePrompts().open(player(), prompt);
  }

  /** Closes the menu for the player who clicked. */
  public void close() {
    player().closeInventory();
  }

  /** The serializer, opener and prompt opener a click acts through, grouped to keep two fields. */
  private record ClickServices(
      MiniMessage miniMessage, MenuOpener opener, AnvilPromptOpener prompts) {

    Component render(String miniMessageText) {
      return miniMessage.deserialize(miniMessageText);
    }

    MenuOpener requireOpener() {
      if (opener == null) {
        throw new IllegalStateException(
            "Navigation is unavailable on this click; declare a MenuClick parameter on a @Button"
                + " method to open menus");
      }
      return opener;
    }

    AnvilPromptOpener requirePrompts() {
      if (prompts == null) {
        throw new IllegalStateException(
            "Prompts are unavailable on this click; declare a MenuClick parameter on a @Button"
                + " method to prompt for text");
      }
      return prompts;
    }
  }
}
