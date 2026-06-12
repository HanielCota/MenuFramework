package dev.haniel.menu.paper.view;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** An openable menu, either static or paginated, registered under a {@code MenuId}. */
public interface PaperMenu {

  /**
   * Opens this menu for the given player, with no open argument.
   *
   * @param player the viewer; never null
   */
  default void open(Player player) {
    open(player, null);
  }

  /**
   * Opens this menu for the given player, injecting the open argument into any {@code @Arg} field.
   *
   * <p>Paginated menus write a non-null {@code argument} into every {@code @Arg} field whose
   * declared type it matches, before the first render; an argument matching no field is a runtime
   * error. Static menus share a single instance and ignore the argument.
   *
   * @param player the viewer; never null
   * @param argument the open argument, or {@code null} for none
   */
  void open(Player player, @Nullable Object argument);
}
