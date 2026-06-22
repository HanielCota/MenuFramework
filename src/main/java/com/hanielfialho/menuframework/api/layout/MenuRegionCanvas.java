package com.hanielfialho.menuframework.api.layout;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import java.util.Objects;
import java.util.function.IntConsumer;
import org.bukkit.inventory.ItemStack;

/**
 * Region-relative facade over a {@link MenuCanvas}.
 *
 * <p>Positions accepted by {@code *At} methods are relative to the region, while consumers passed
 * to {@link #forEachSlot(IntConsumer)} receive raw menu slots.
 *
 * @param <S> menu-state type
 */
public final class MenuRegionCanvas<S> {

  private final MenuCanvas<S> canvas;
  private final SlotRegion region;

  /**
   * Creates a bound region facade.
   *
   * @param canvas target canvas
   * @param region target region
   */
  public MenuRegionCanvas(MenuCanvas<S> canvas, SlotRegion region) {
    this.canvas = Objects.requireNonNull(canvas, "canvas");
    this.region = Objects.requireNonNull(region, "region");
    this.region.forEachSlot(this.canvas.layout()::checkSlot);
  }

  /**
   * Returns the bound region.
   *
   * @return immutable region
   */
  public SlotRegion region() {
    return this.region;
  }

  /**
   * Returns the number of positions in the region.
   *
   * @return positive size
   */
  public int size() {
    return this.region.size();
  }

  /**
   * Assigns an item at a region-relative position.
   *
   * @param position zero-based region position
   * @param icon icon
   */
  public void itemAt(int position, ItemStack icon) {
    this.canvas.item(this.region.slot(position), icon);
  }

  /**
   * Assigns a button at a region-relative position.
   *
   * @param position zero-based region position
   * @param icon icon
   * @param clickHandler handler
   */
  public void buttonAt(int position, ItemStack icon, MenuClickHandler<S> clickHandler) {
    this.canvas.button(this.region.slot(position), icon, clickHandler);
  }

  /**
   * Marks one region-relative position as explicitly empty.
   *
   * @param position zero-based region position
   */
  public void emptyAt(int position) {
    this.canvas.empty(this.region.slot(position));
  }

  /**
   * Assigns the same visual item to every slot.
   *
   * <p>The underlying canvas defensively copies the icon for every assignment.
   *
   * @param icon fill icon
   */
  public void fill(ItemStack icon) {
    Objects.requireNonNull(icon, "icon");
    this.region.forEachSlot(slot -> this.canvas.item(slot, icon));
  }

  /** Marks every slot as explicitly empty. */
  public void empty() {
    this.region.forEachSlot(this.canvas::empty);
  }

  /**
   * Iterates over raw menu slots in region order.
   *
   * @param consumer raw-slot consumer
   */
  public void forEachSlot(IntConsumer consumer) {
    this.region.forEachSlot(consumer);
  }
}
