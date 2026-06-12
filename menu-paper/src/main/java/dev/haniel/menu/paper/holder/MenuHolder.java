package dev.haniel.menu.paper.holder;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.MenuTemplate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link org.bukkit.inventory.InventoryHolder} that identifies a menu view and routes its
 * clicks.
 *
 * <p>A click is matched to its menu through this holder, so no {@code Map<UUID, Menu>} is needed.
 * The inventory is filled from the template's pre-built items; opening allocates no items of its
 * own.
 */
public final class MenuHolder implements ClickableHolder, OpenMenu {

  private final MenuId menuId;
  private final MenuTemplate<ItemStack> template;
  private final Inventory inventory;

  /**
   * Builds a view of the given template with the given title, showing every button.
   *
   * @param menuId the id of the menu being shown; never null
   * @param template the shared, pre-rendered blueprint; never null
   * @param title the inventory title; never null
   */
  public MenuHolder(MenuId menuId, MenuTemplate<ItemStack> template, Component title) {
    this(menuId, template, title, Set.of());
  }

  /**
   * Builds a view of the given template, leaving the hidden slots empty for this viewer.
   *
   * @param menuId the id of the menu being shown; never null
   * @param template the shared, pre-rendered blueprint; never null
   * @param title the inventory title; never null
   * @param hidden the slots to leave empty (their {@code @Visible} rule denied them); never null
   */
  public MenuHolder(
      MenuId menuId, MenuTemplate<ItemStack> template, Component title, Set<Integer> hidden) {
    this.menuId = Objects.requireNonNull(menuId, "menuId");
    this.template = Objects.requireNonNull(template, "template");
    this.inventory =
        build(Objects.requireNonNull(title, "title"), Objects.requireNonNull(hidden, "hidden"));
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }

  @Override
  public MenuId menuId() {
    return menuId;
  }

  /** A static menu has no dynamic content, so a refresh has nothing to recompute. */
  @Override
  public void refresh() {
    // no-op
  }

  /**
   * Routes a click at the given raw slot to its bound action, if any.
   *
   * <p>A slot left empty for this viewer by a {@code @Visible} rule has no rendered item, so its
   * action is skipped — a hidden button is non-clickable.
   *
   * @param rawSlot the raw slot from the click event
   * @param context the click context handed to the action; never null
   */
  public void click(int rawSlot, ClickContext context) {
    if (isRendered(rawSlot)) {
      template.actionAt(rawSlot).ifPresent(action -> action.onClick(context));
    }
  }

  private boolean isRendered(int rawSlot) {
    return rawSlot >= 0 && rawSlot < inventory.getSize() && inventory.getItem(rawSlot) != null;
  }

  private Inventory build(Component title, Set<Integer> hidden) {
    Inventory created = Bukkit.createInventory(this, template.size(), title);
    render(created, hidden);
    return created;
  }

  private void render(Inventory target, Set<Integer> hidden) {
    IntStream.range(0, template.size())
        .filter(slot -> !hidden.contains(slot))
        .forEach(slot -> renderSlot(target, slot));
  }

  private void renderSlot(Inventory target, int slot) {
    template.iconAt(slot).ifPresent(item -> target.setItem(slot, item));
  }
}
