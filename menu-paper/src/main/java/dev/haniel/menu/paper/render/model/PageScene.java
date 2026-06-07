package dev.haniel.menu.paper.render.model;

import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.PagedContent;
import dev.haniel.menu.template.PagedDecor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * The per-view inputs a {@link dev.haniel.menu.paper.render.PageRenderer} needs: shared appearance
 * plus this view's bound content and overlay.
 *
 * @param id the menu id, used in the cache key
 * @param title the deserialized inventory title
 * @param size the total slot count
 * @param layout the resolved mask layout
 * @param decor the pre-rendered navigation and border
 * @param content the bound content source and renderer
 * @param overlay the bound static buttons
 */
public record PageScene(
    MenuId id,
    Component title,
    int size,
    MaskLayout layout,
    PagedDecor<ItemStack> decor,
    PagedContent<ItemStack> content,
    Overlay overlay) {

  /**
   * Returns the number of content slots per page.
   *
   * @return the page capacity
   */
  public int perPage() {
    return layout.contentSlotCount();
  }
}
