package dev.haniel.menu.template;

import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import java.util.Map;

/**
 * The shared, instance-free appearance of a paginated menu, built once at boot.
 *
 * <p>Content visuals are rendered per page at runtime; everything here (navigation, border and any
 * static overlay buttons) is fixed and reused by every player and page.
 *
 * @param id the menu id
 * @param title the raw MiniMessage title string
 * @param layout the resolved mask layout
 * @param decor the pre-rendered navigation and border visuals
 * @param overlayVisuals the pre-rendered static button visuals, by slot
 * @param <V> the platform visual type
 */
public record PagedAppearance<V>(
    MenuId id,
    String title,
    MaskLayout layout,
    PagedDecor<V> decor,
    Map<Integer, V> overlayVisuals) {

  public PagedAppearance {
    overlayVisuals = Map.copyOf(overlayVisuals);
  }

  /**
   * Returns the number of content slots per page.
   *
   * @return the page capacity
   */
  public int perPage() {
    return layout.contentSlotCount();
  }

  /**
   * Returns the total number of slots.
   *
   * @return {@code rows * 9}
   */
  public int size() {
    return layout.size();
  }
}
