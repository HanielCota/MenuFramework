package dev.haniel.menu.paper.listener;

import dev.haniel.menu.click.ClickType;

/** Maps a Bukkit click into the framework's {@link ClickType}. */
public final class ClickTypeMapper {

  private ClickTypeMapper() {}

  /**
   * Maps the given Bukkit click type.
   *
   * @param click the Bukkit click type; never null
   * @return the matching framework click type, or {@link ClickType#OTHER}
   */
  public static ClickType map(org.bukkit.event.inventory.ClickType click) {
    return switch (click) {
      case LEFT -> ClickType.LEFT;
      case RIGHT -> ClickType.RIGHT;
      case SHIFT_LEFT -> ClickType.SHIFT_LEFT;
      case SHIFT_RIGHT -> ClickType.SHIFT_RIGHT;
      case MIDDLE -> ClickType.MIDDLE;
      case DROP, CONTROL_DROP -> ClickType.DROP;
      case DOUBLE_CLICK -> ClickType.DOUBLE_CLICK;
      case NUMBER_KEY -> ClickType.NUMBER_KEY;
      case SWAP_OFFHAND -> ClickType.SWAP_OFFHAND;
      default -> ClickType.OTHER;
    };
  }
}
