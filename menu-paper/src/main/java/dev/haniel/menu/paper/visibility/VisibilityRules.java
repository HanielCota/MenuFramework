package dev.haniel.menu.paper.visibility;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.entity.Player;

/**
 * The {@code @Visible} per-viewer rules of a menu class, resolved once and cached.
 *
 * <p>Resolution lives in {@link VisibilityReader}; this type holds the rules by button id, caches
 * them per class, and evaluates them for one viewer against the menu's button-to-slot map. Mirrors
 * the {@code @OnOpen}/{@code @OnClose} reflection in the hook package: it lives in the Paper layer
 * because a rule may accept a Bukkit {@code Player}.
 */
public final class VisibilityRules {

  private static final ConcurrentMap<Class<?>, VisibilityRules> CACHE = new ConcurrentHashMap<>();

  private final Map<String, Rule> rules;

  VisibilityRules(Map<String, Rule> rules) {
    this.rules = Objects.requireNonNull(rules, "rules");
  }

  /**
   * Returns the cached rules for the given menu class, reading them on first use.
   *
   * @param type the menu class; never null
   * @return the resolved rules
   * @throws InvalidMenuException if a {@code @Visible} method has an unsupported signature
   */
  public static VisibilityRules of(Class<?> type) {
    return CACHE.computeIfAbsent(type, VisibilityReader::read);
  }

  /**
   * Returns whether the class declares no {@code @Visible} rules.
   *
   * @return {@code true} if there is nothing to evaluate
   */
  public boolean isEmpty() {
    return rules.isEmpty();
  }

  /**
   * Computes the slots to hide for the given viewer.
   *
   * <p>Each rule is evaluated against the menu instance and the viewer; a rule returning {@code
   * false} hides its button's slot. Every rule's button id must map to a real slot.
   *
   * @param instance the menu instance the rules are declared on; never null
   * @param viewer the player about to view the menu; never null
   * @param buttonSlots the button-id-to-slot map of the menu; never null
   * @return the slots whose button is hidden for this viewer; empty if all are shown
   * @throws InvalidMenuException if a {@code @Visible} id matches no button
   */
  public Set<Integer> hiddenSlots(
      Object instance, Player viewer, Map<String, Integer> buttonSlots) {
    Set<Integer> hidden = new HashSet<>();
    rules.forEach(
        (buttonId, rule) -> collectHidden(buttonId, rule, instance, viewer, buttonSlots, hidden));
    return hidden;
  }

  private void collectHidden(
      String buttonId,
      Rule rule,
      Object instance,
      Player viewer,
      Map<String, Integer> buttonSlots,
      Set<Integer> hidden) {
    Integer slot = buttonSlots.get(buttonId);
    if (slot == null) {
      throw new InvalidMenuException(
          "@Visible references unknown button '" + buttonId + "'; add a @Button with that id");
    }
    if (!rule.test(instance, viewer)) {
      hidden.add(slot);
    }
  }

  /** One {@code @Visible} method bound by handle, recording whether it wants the viewer. */
  record Rule(MethodHandle handle, boolean acceptsPlayer) {

    boolean test(Object instance, Player viewer) {
      try {
        Object result =
            acceptsPlayer
                ? handle.invokeWithArguments(instance, viewer)
                : handle.invokeWithArguments(instance);
        return (boolean) result;
      } catch (Throwable failure) {
        throw new VisibilityException(
            "A @Visible rule threw while deciding button visibility", failure);
      }
    }
  }
}
