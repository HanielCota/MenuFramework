package dev.haniel.menu.config;

import java.util.List;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * The appearance of a menu's pagination, read from YAML.
 *
 * <p>The mask lays out the grid declaratively (see {@code MaskLayout}); the navigation buttons
 * supply only the look of the previous/next controls (their slots come from the mask).
 *
 * @param mask one string per row; each character is a slot role
 * @param previousButton the look of the previous-page control
 * @param nextButton the look of the next-page control
 */
@ConfigSerializable
public record PaginationConfig(
    List<String> mask, ButtonConfig previousButton, ButtonConfig nextButton) {

  public PaginationConfig {
    mask = (mask == null) ? List.of() : List.copyOf(mask);
  }

  /**
   * Returns whether no pagination section was effectively configured.
   *
   * <p>The mask is what makes a menu paginated: navigation buttons take their slots from it, so a
   * section with buttons but no mask cannot paginate and is treated as empty.
   *
   * @return {@code true} when the mask is absent
   */
  public boolean isEmpty() {
    return mask.isEmpty();
  }
}
