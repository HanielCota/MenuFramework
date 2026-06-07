package dev.haniel.menu.compiler.model;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.MenuTemplate;

/**
 * A compiled static menu: a fixed, pre-rendered template.
 *
 * @param id the logical id of the menu
 * @param title the MiniMessage title string
 * @param template the immutable, pre-rendered template
 * @param <V> the platform visual type
 */
public record CompiledStaticMenu<V>(MenuId id, String title, MenuTemplate<V> template)
    implements CompiledMenu<V> {

  @Override
  public <R> R accept(CompiledMenuVisitor<V, R> visitor) {
    return visitor.visitStatic(this);
  }
}
