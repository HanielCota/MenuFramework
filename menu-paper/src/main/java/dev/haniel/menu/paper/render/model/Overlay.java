package dev.haniel.menu.paper.render.model;

import dev.haniel.menu.action.MenuAction;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 * The static buttons drawn over a paginated menu, by slot.
 *
 * <p>Visuals are pre-rendered once; actions are bound to the per-player instance at open. Both are
 * placed on every page, on top of the border.
 *
 * @param visuals the button item per slot
 * @param actions the button action per slot
 */
public record Overlay(Map<Integer, ItemStack> visuals, Map<Integer, MenuAction> actions) {

  public Overlay {
    visuals = Map.copyOf(visuals);
    actions = Map.copyOf(actions);
  }
}
