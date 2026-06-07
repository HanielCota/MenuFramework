package dev.haniel.menu.paper.holder;

import dev.haniel.menu.click.ClickContext;
import org.bukkit.inventory.InventoryHolder;

/**
 * An {@link InventoryHolder} that can route a raw-slot click to its own logic.
 *
 * <p>Implemented by both static and paginated holders so the single listener can delegate without
 * knowing the concrete menu kind.
 */
public interface ClickableHolder extends InventoryHolder {

  /**
   * Routes a click at the given raw slot.
   *
   * @param rawSlot the raw slot from the click event
   * @param context the click context handed to any action; never null
   */
  void click(int rawSlot, ClickContext context);
}
