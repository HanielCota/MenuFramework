package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/**
 * Implementação interna, mutável e de uso único do canvas de renderização.
 *
 * @param <S> tipo de estado da sessão
 */
public final class DefaultMenuCanvas<S> implements MenuCanvas<S> {

  private final MenuLayout layout;
  private final List<MenuSlot<S>> slots;
  private final BitSet assignedSlots;

  private MenuSlot<S> background;
  private boolean built;

  public DefaultMenuCanvas(MenuLayout layout) {
    this.layout = Objects.requireNonNull(layout, "layout");
    this.slots = new ArrayList<>(Collections.nCopies(layout.size(), null));
    this.assignedSlots = new BitSet(layout.size());
  }

  @Override
  public MenuLayout layout() {
    return this.layout;
  }

  @Override
  public void item(int slot, ItemStack icon) {
    this.write(slot, MenuSlot.item(icon));
  }

  @Override
  public void button(int slot, ItemStack icon, MenuClickHandler<S> clickHandler) {
    this.write(slot, MenuSlot.button(icon, clickHandler));
  }

  @Override
  public void empty(int slot) {
    this.ensureMutable();
    this.layout.checkSlot(slot);
    this.ensureNotAssigned(slot);
    this.assignedSlots.set(slot);
  }

  @Override
  public void background(ItemStack icon) {
    this.ensureMutable();

    if (this.background != null) {
      throw new IllegalStateException("A background has already been configured for this frame");
    }

    this.background = MenuSlot.item(icon);
  }

  public MenuFrame<S> build() {
    this.ensureMutable();
    this.built = true;

    List<MenuSlot<S>> snapshot = new ArrayList<>(this.slots);

    if (this.background != null) {
      for (int slot = 0; slot < this.layout.size(); slot++) {
        if (!this.assignedSlots.get(slot)) {
          snapshot.set(slot, this.background);
        }
      }
    }

    return new MenuFrame<>(this.layout, snapshot);
  }

  private void write(int slot, MenuSlot<S> menuSlot) {
    this.ensureMutable();
    this.layout.checkSlot(slot);
    this.ensureNotAssigned(slot);

    this.slots.set(slot, Objects.requireNonNull(menuSlot, "menuSlot"));
    this.assignedSlots.set(slot);
  }

  private void ensureNotAssigned(int slot) {
    if (this.assignedSlots.get(slot)) {
      throw new IllegalStateException(
          "Slot " + slot + " was assigned more than once in the same render");
    }
  }

  private void ensureMutable() {
    if (this.built) {
      throw new IllegalStateException("This canvas has already been built");
    }
  }
}
