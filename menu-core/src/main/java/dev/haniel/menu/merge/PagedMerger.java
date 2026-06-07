package dev.haniel.menu.merge;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.config.ButtonConfig;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.config.PaginationConfig;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.Slot;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedDecor;
import dev.haniel.menu.template.PagedWiring;
import java.util.HashMap;
import java.util.Map;

/**
 * Joins a paginated {@link PagedStructure} with its {@link MenuConfig} appearance.
 *
 * <p>Resolves the mask, pre-renders navigation, border and any static overlay buttons (whose looks
 * come from the {@code buttons} section) and keeps the unbound provider, button actions and states
 * for per-open binding. An annotated button missing from the YAML is a boot error.
 *
 * @param <V> the platform visual type built by the {@link IconFactory}
 */
public final class PagedMerger<V> {

  private static final Icon BORDER = Icon.of("GRAY_STAINED_GLASS_PANE").named(" ");

  private final IconFactory<V> icons;

  /**
   * Creates a merger that renders visuals through the given factory.
   *
   * @param icons the factory used to build each visual; never null
   */
  public PagedMerger(IconFactory<V> icons) {
    this.icons = icons;
  }

  /**
   * Joins the structure and config into a paginated menu.
   *
   * @param structure the boot-discovered structure; never null
   * @param config the appearance read from YAML; never null
   * @return the merged paginated menu
   * @throws InvalidMenuException if behaviour and appearance do not line up
   */
  public CompiledPagedMenu<V> merge(PagedStructure structure, MenuConfig config) {
    PaginationConfig pagination = requirePagination(structure, config);
    MaskLayout layout = MaskLayout.resolve(pagination.mask(), config.rows());
    ensureOverlayDoesNotReplaceDynamicSlots(config, layout);
    PagedAppearance<V> appearance = appearance(structure, config, pagination, layout);
    return new CompiledPagedMenu<>(appearance, wiring(structure, config));
  }

  private PagedAppearance<V> appearance(
      PagedStructure structure, MenuConfig config, PaginationConfig pagination, MaskLayout layout) {
    return new PagedAppearance<>(
        structure.id(), config.title(), layout, decor(layout, pagination), overlayVisuals(config));
  }

  private PagedWiring wiring(PagedStructure structure, MenuConfig config) {
    return new PagedWiring(
        structure.instantiator(),
        structure.provider(),
        overlayActions(structure, config),
        structure.states());
  }

  private PagedDecor<V> decor(MaskLayout layout, PaginationConfig pagination) {
    requireNav(layout.previousSlot(), pagination.previousButton(), '<');
    requireNav(layout.nextSlot(), pagination.nextButton(), '>');
    V previous = navigation(layout.previousSlot(), pagination.previousButton());
    V next = navigation(layout.nextSlot(), pagination.nextButton());
    return new PagedDecor<>(previous, next, icons.create(BORDER));
  }

  private V navigation(int slot, ButtonConfig button) {
    if (slot < 0) {
      return null;
    }
    return icons.create(button.icon());
  }

  private Map<Integer, V> overlayVisuals(MenuConfig config) {
    Map<Integer, V> visuals = new HashMap<>();
    config.buttons().forEach((id, button) -> place(visuals, button, config));
    return visuals;
  }

  private void place(Map<Integer, V> visuals, ButtonConfig button, MenuConfig config) {
    int slot = slot(button, config);
    if (visuals.putIfAbsent(slot, icons.create(button.icon())) != null) {
      throw new InvalidMenuException("Slot " + slot + " is used by more than one button");
    }
  }

  private Map<Integer, UnboundAction> overlayActions(PagedStructure structure, MenuConfig config) {
    ensureButtonsConfigured(structure, config);
    Map<Integer, UnboundAction> actions = new HashMap<>();
    structure.buttons().forEach((id, action) -> actions.put(slotOf(id, config), action));
    return actions;
  }

  private void ensureButtonsConfigured(PagedStructure structure, MenuConfig config) {
    structure.buttons().keySet().stream()
        .filter(id -> !config.buttons().containsKey(id.value()))
        .findFirst()
        .ifPresent(this::failMissingButton);
  }

  private void ensureOverlayDoesNotReplaceDynamicSlots(MenuConfig config, MaskLayout layout) {
    config
        .buttons()
        .forEach(
            (id, button) -> {
              int slot = slot(button, config);
              if (contains(layout.contentSlots(), slot)) {
                throw new InvalidMenuException(
                    "Button '" + id + "' cannot use content slot " + slot + " in pagination mask");
              }
              if (slot == layout.previousSlot() || slot == layout.nextSlot()) {
                throw new InvalidMenuException(
                    "Button '"
                        + id
                        + "' cannot use navigation slot "
                        + slot
                        + " in pagination mask");
              }
            });
  }

  private boolean contains(int[] slots, int wanted) {
    for (int slot : slots) {
      if (slot == wanted) {
        return true;
      }
    }
    return false;
  }

  private void failMissingButton(ButtonId id) {
    throw new InvalidMenuException(
        "Button '" + id.value() + "' is annotated but missing in YAML; add buttons." + id.value());
  }

  private PaginationConfig requirePagination(PagedStructure structure, MenuConfig config) {
    return config
        .paginationConfig()
        .orElseThrow(
            () ->
                new InvalidMenuException(
                    "Menu '"
                        + structure.id().value()
                        + "' is @Paginated but YAML has no 'pagination'"));
  }

  private void requireNav(int slot, ButtonConfig button, char role) {
    if (slot >= 0 && button == null) {
      throw new InvalidMenuException(
          "mask uses '" + role + "' but its navigation button is missing");
    }
  }

  private int slotOf(ButtonId id, MenuConfig config) {
    return slot(config.buttons().get(id.value()), config);
  }

  private int slot(ButtonConfig button, MenuConfig config) {
    try {
      return Slot.of(button.slot(), config.rows()).value();
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "Button slot " + button.slot() + " is outside the menu bounds");
    }
  }
}
