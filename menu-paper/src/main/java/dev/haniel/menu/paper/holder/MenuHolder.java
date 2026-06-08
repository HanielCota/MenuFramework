package dev.haniel.menu.paper.holder;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.MenuTemplate;
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
   * Builds a view of the given template with the given title.
   *
   * @param menuId the id of the menu being shown; never null
   * @param template the shared, pre-rendered blueprint; never null
   * @param title the inventory title; never null
   */
  public MenuHolder(MenuId menuId, MenuTemplate<ItemStack> template, Component title) {
    this.menuId = menuId;
    this.template = template;
    this.inventory = build(title);
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
   * @param rawSlot the raw slot from the click event
   * @param context the click context handed to the action; never null
   */
  public void click(int rawSlot, ClickContext context) {
    template.actionAt(rawSlot).ifPresent(action -> action.onClick(context));
  }

  private Inventory build(Component title) {
    Inventory created = Bukkit.createInventory(this, template.size(), title);
    render(created);
    return created;
  }

  private void render(Inventory target) {
    IntStream.range(0, template.size()).forEach(slot -> renderSlot(target, slot));
  }

  private void renderSlot(Inventory target, int slot) {
    template.iconAt(slot).ifPresent(item -> target.setItem(slot, item));
  }
}
