package dev.haniel.menu.paper.hook;

import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 * The {@code @OnOpen}/{@code @OnClose} handlers bound to one open view, ready to fire.
 *
 * <p>Created per open from {@link HookDefinitions#bind(Object)}. Firing runs every handler on the
 * caller's thread, which is the view's owning thread.
 */
public final class MenuHooks {

  private final List<Consumer<Player>> onOpen;
  private final List<Consumer<Player>> onClose;

  MenuHooks(List<Consumer<Player>> onOpen, List<Consumer<Player>> onClose) {
    this.onOpen = onOpen;
    this.onClose = onClose;
  }

  /**
   * Runs every {@code @OnOpen} handler for the viewer.
   *
   * @param player the viewer; never null
   */
  public void fireOpen(Player player) {
    onOpen.forEach(handler -> handler.accept(player));
  }

  /**
   * Runs every {@code @OnClose} handler for the viewer.
   *
   * @param player the viewer; never null
   */
  public void fireClose(Player player) {
    onClose.forEach(handler -> handler.accept(player));
  }
}
