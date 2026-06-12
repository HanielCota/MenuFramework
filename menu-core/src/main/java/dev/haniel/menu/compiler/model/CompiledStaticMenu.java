package dev.haniel.menu.compiler.model;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.MenuTemplate;
import java.util.Map;

/**
 * A compiled static menu: a fixed, pre-rendered template.
 *
 * @param id the logical id of the menu
 * @param title the MiniMessage title string
 * @param template the immutable, pre-rendered template
 * @param buttonSlots the button id to slot map, for per-viewer {@code @Visible} rules
 * @param <V> the platform visual type
 */
public record CompiledStaticMenu<V>(
    MenuId id, String title, MenuTemplate<V> template, Map<String, Integer> buttonSlots)
    implements CompiledMenu<V> {

  public CompiledStaticMenu {
    buttonSlots = Map.copyOf(buttonSlots);
  }

  /**
   * Creates a static menu with no button-to-slot map (no {@code @Visible} rules).
   *
   * @param id the logical id of the menu
   * @param title the MiniMessage title string
   * @param template the immutable, pre-rendered template
   */
  public CompiledStaticMenu(MenuId id, String title, MenuTemplate<V> template) {
    this(id, title, template, Map.of());
  }

  @Override
  public <R> R accept(CompiledMenuVisitor<V, R> visitor) {
    return visitor.visitStatic(this);
  }
}
