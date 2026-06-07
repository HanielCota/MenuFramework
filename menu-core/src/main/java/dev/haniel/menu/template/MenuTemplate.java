package dev.haniel.menu.template;

import dev.haniel.menu.action.MenuAction;
import java.util.Arrays;
import java.util.Optional;

/**
 * The immutable, shared blueprint of a menu.
 *
 * <p>Holds the pre-built visuals and a slot-indexed action array, so a click resolves its
 * action in O(1) by raw slot, with bounds guarded. Visuals are built once during the merge and
 * reused by every open view; per-player state lives elsewhere.
 *
 * @param <V> the platform visual type (an {@code ItemStack} on Paper)
 */
public final class MenuTemplate<V> {

  private final Object[] icons;
  private final MenuAction[] actions;

  /**
   * Creates a template from the pre-built visuals and the click bindings.
   *
   * @param icons the visual per slot, indexed by slot; copied defensively
   * @param bindings the click bindings; flattened into a slot-indexed array
   */
  public MenuTemplate(Object[] icons, SlotBinding[] bindings) {
    this.icons = icons.clone();
    this.actions = index(bindings, icons.length);
  }

  /**
   * Returns the number of slots in this menu.
   *
   * @return the slot count
   */
  public int size() {
    return icons.length;
  }

  /**
   * Finds the visual at the given slot.
   *
   * @param slot the slot index
   * @return the visual, or empty if out of bounds or unset
   */
  @SuppressWarnings("unchecked") // icons only ever holds V instances placed by the merger
  public Optional<V> iconAt(int slot) {
    if (isOutOfBounds(slot)) {
      return Optional.empty();
    }
    return Optional.ofNullable((V) icons[slot]);
  }

  /**
   * Finds the action bound to the given slot.
   *
   * @param slot the slot index
   * @return the action, or empty if out of bounds or unbound
   */
  public Optional<MenuAction> actionAt(int slot) {
    if (isOutOfBounds(slot)) {
      return Optional.empty();
    }
    return Optional.ofNullable(actions[slot]);
  }

  private boolean isOutOfBounds(int slot) {
    return slot < 0 || slot >= actions.length;
  }

  private static MenuAction[] index(SlotBinding[] bindings, int size) {
    MenuAction[] indexed = new MenuAction[size];
    Arrays.stream(bindings).forEach(binding -> indexed[binding.slot()] = binding.action());
    return indexed;
  }
}
