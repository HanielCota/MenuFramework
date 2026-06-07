package dev.haniel.menu.compiler.model;

import dev.haniel.menu.domain.MenuId;
import java.util.List;

/**
 * The behaviour read from an annotated static menu class: its id and button actions.
 *
 * @param id the menu id from {@code @Menu}
 * @param behaviors the button behaviours from the {@code @Button} methods
 */
public record MenuBlueprint(MenuId id, List<ButtonBehavior> behaviors) {

  public MenuBlueprint {
    behaviors = List.copyOf(behaviors);
  }
}
