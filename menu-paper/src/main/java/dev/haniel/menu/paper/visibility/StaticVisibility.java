package dev.haniel.menu.paper.visibility;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.entity.Player;

/**
 * The {@code @Visible} rules of a static menu, bound once to its shared instance.
 *
 * <p>A static menu has no per-open instance — one shared instance backs every view — so its
 * visibility is bound at registration and evaluated per open against the viewer. Paginated menus
 * evaluate {@link VisibilityRules} directly on their fresh per-open instance instead.
 */
public final class StaticVisibility {

  private static final StaticVisibility NONE = new StaticVisibility(viewer -> Set.of());

  private final Function<Player, Set<Integer>> hiddenSlots;

  private StaticVisibility(Function<Player, Set<Integer>> hiddenSlots) {
    this.hiddenSlots = hiddenSlots;
  }

  /**
   * Returns the rule that hides nothing, for a menu with no {@code @Visible} methods.
   *
   * @return the empty visibility
   */
  public static StaticVisibility none() {
    return NONE;
  }

  /**
   * Binds the class's {@code @Visible} rules to the shared instance and button slots.
   *
   * @param type the menu class; never null
   * @param instance the shared menu instance the rules are declared on; never null
   * @param buttonSlots the button-id-to-slot map of the menu; never null
   * @return the bound visibility, or {@link #none()} if the class declares no rules
   */
  public static StaticVisibility of(
      Class<?> type, Object instance, Map<String, Integer> buttonSlots) {
    Objects.requireNonNull(instance, "instance");
    VisibilityRules rules = VisibilityRules.of(type);
    if (rules.isEmpty()) {
      return NONE;
    }
    Map<String, Integer> slots = Map.copyOf(buttonSlots);
    return new StaticVisibility(viewer -> rules.hiddenSlots(instance, viewer, slots));
  }

  /**
   * Computes the slots to hide for the given viewer.
   *
   * @param viewer the player about to open the menu; never null
   * @return the hidden slots; empty if all buttons are shown
   */
  public Set<Integer> hiddenSlots(Player viewer) {
    return hiddenSlots.apply(viewer);
  }
}
