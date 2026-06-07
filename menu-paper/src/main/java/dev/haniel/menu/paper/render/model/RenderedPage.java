package dev.haniel.menu.paper.render.model;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.PageNumber;
import org.bukkit.inventory.ItemStack;

/**
 * A fully resolved page ready to be written into an inventory.
 *
 * <p>{@code slots} holds the item for every inventory slot (content, border and navigation), {@code
 * null} for empty slots. {@code actions} holds the click action per content slot, {@code null}
 * elsewhere — these come straight from the provider, never the cache.
 *
 * @param page the effective page after clamping
 * @param slots the item per slot, indexed by slot
 * @param actions the action per slot, indexed by slot
 * @param hasPrevious whether the rendered page has a previous page
 * @param hasNext whether the rendered page has a next page
 */
public record RenderedPage(
    PageNumber page,
    ItemStack[] slots,
    MenuAction[] actions,
    boolean hasPrevious,
    boolean hasNext) {}
