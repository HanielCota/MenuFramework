package dev.haniel.menu.merge;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.model.ButtonBehavior;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.MenuTemplate;
import dev.haniel.menu.template.SlotBinding;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Joins a static {@link MenuBlueprint} with its {@link MenuConfig} appearance.
 *
 * <p>Every configured button is rendered into its slot and an annotated one also gets a click
 * binding; an annotated button missing from the YAML is a boot error. Paginated menus are handled
 * by {@link PagedMerger}.
 *
 * @param <V> the platform visual type built by the {@link IconFactory}
 */
public final class StaticMerger<V> {

  private final IconFactory<V> icons;

  /**
   * Creates a merger that renders visuals through the given factory.
   *
   * @param icons the factory used to build each visual; never null
   */
  public StaticMerger(IconFactory<V> icons) {
    this.icons = Objects.requireNonNull(icons, "icons");
  }

  /**
   * Merges the blueprint and config into a compiled static menu.
   *
   * @param blueprint the behaviour read from the annotated class; never null
   * @param config the appearance read from YAML; never null
   * @return the compiled static menu, ready to open
   * @throws InvalidMenuException if an annotated button has no configuration entry
   */
  public CompiledMenu<V> merge(MenuBlueprint blueprint, MenuConfig config) {
    MenuTemplate<V> template = new MenuTemplate<>(visuals(config), bindings(blueprint, config));
    return new CompiledStaticMenu<>(
        blueprint.id(), config.title(), template, buttonSlots(blueprint, config));
  }

  private Map<String, Integer> buttonSlots(MenuBlueprint blueprint, MenuConfig config) {
    Map<String, Integer> slots = new HashMap<>();
    blueprint
        .behaviors()
        .forEach(
            behavior ->
                slots.put(
                    behavior.id().value(),
                    MergeButtons.slot(require(behavior.id(), config), config)));
    return slots;
  }

  private Object[] visuals(MenuConfig config) {
    Object[] visuals = new Object[config.size()];
    config.buttons().forEach((id, button) -> place(visuals, button, config));
    return visuals;
  }

  private void place(Object[] visuals, ButtonConfig button, MenuConfig config) {
    int slot = MergeButtons.slot(button, config);
    if (visuals[slot] != null) {
      throw new InvalidMenuException("Slot " + slot + " is used by more than one button");
    }
    visuals[slot] = icons.create(button.icon());
  }

  private SlotBinding[] bindings(MenuBlueprint blueprint, MenuConfig config) {
    return blueprint.behaviors().stream()
        .map(behavior -> bind(behavior, config))
        .toArray(SlotBinding[]::new);
  }

  private SlotBinding bind(ButtonBehavior behavior, MenuConfig config) {
    ButtonConfig button = require(behavior.id(), config);
    return new SlotBinding(MergeButtons.slot(button, config), behavior.action());
  }

  private ButtonConfig require(ButtonId id, MenuConfig config) {
    ButtonConfig button = config.buttons().get(id.value());
    if (button == null) {
      throw MergeButtons.missingButton(id.value());
    }
    return button;
  }
}
