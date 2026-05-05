package com.github.hanielcota.menuframework.api;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * A provider for dynamic content in paginated menus. Allows for reactive and potentially
 * asynchronous loading of items.
 */
@FunctionalInterface
public interface DynamicContentProvider {

  /** Creates a static provider from a fixed list. */
  static DynamicContentProvider of(@NonNull List<SlotDefinition> items) {
    Objects.requireNonNull(items, "items");
    var defensiveCopy = List.copyOf(items);
    return (player, session) -> defensiveCopy;
  }

  /**
   * Provides a list of slot definitions for the given player and menu session.
   *
   * @param player the player viewing the menu
   * @param session the current menu session
   * @return a list of slot definitions
   */
  @NonNull List<SlotDefinition> provide(@NonNull Player player, @NonNull MenuSession session);
}
