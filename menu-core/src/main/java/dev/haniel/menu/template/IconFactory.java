package dev.haniel.menu.template;

import dev.haniel.menu.item.Icon;

/**
 * Builds the platform visual for an {@link Icon}.
 *
 * <p>Invoked during the merge for static items and navigation, and per page for paginated
 * content. The Paper layer implements this to produce {@code ItemStack}s; the domain stays
 * unaware of the concrete visual type {@code V}.
 *
 * @param <V> the platform visual type (an {@code ItemStack} on Paper)
 */
@FunctionalInterface
public interface IconFactory<V> {

  /**
   * Builds the visual for the given icon.
   *
   * @param icon the appearance; never null
   * @return the built visual
   */
  V create(Icon icon);
}
