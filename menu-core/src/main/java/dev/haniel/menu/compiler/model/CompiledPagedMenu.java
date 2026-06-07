package dev.haniel.menu.compiler.model;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedWiring;

/**
 * A compiled paginated menu: shared appearance plus instance-free wiring.
 *
 * <p>One per registered menu, shared across players. The platform layer binds the wiring to a fresh
 * per-player instance on each open.
 *
 * @param appearance the shared, pre-rendered appearance
 * @param wiring the unbound provider, buttons and states
 * @param <V> the platform visual type
 */
public record CompiledPagedMenu<V>(PagedAppearance<V> appearance, PagedWiring wiring)
    implements CompiledMenu<V> {

  @Override
  public MenuId id() {
    return appearance.id();
  }

  @Override
  public String title() {
    return appearance.title();
  }

  @Override
  public <R> R accept(CompiledMenuVisitor<V, R> visitor) {
    return visitor.visitPaged(this);
  }
}
