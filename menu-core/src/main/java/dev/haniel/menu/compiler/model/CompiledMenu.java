package dev.haniel.menu.compiler.model;

import dev.haniel.menu.domain.MenuId;

/**
 * The result of merging behaviour and appearance, either static or paginated.
 *
 * <p>Sealed so the platform layer can dispatch over the two variants exhaustively. The title is
 * the raw MiniMessage string; the platform deserializes it when a view is opened.
 *
 * @param <V> the platform visual type (an {@code ItemStack} on Paper)
 */
public sealed interface CompiledMenu<V> permits CompiledStaticMenu, CompiledPagedMenu {

  /**
   * Returns the logical id of the menu.
   *
   * @return the menu id
   */
  MenuId id();

  /**
   * Returns the raw MiniMessage title string.
   *
   * @return the title string
   */
  String title();

  /**
   * Dispatches this compiled menu to the matching visitor method.
   *
   * @param visitor the variant handler
   * @param <R> the result type
   * @return the visitor result
   */
  <R> R accept(CompiledMenuVisitor<V, R> visitor);
}
