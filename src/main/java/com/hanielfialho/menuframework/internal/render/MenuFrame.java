package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Snapshot interno e imutável de todos os slots renderizados.
 *
 * @param <S> tipo de estado da sessão
 */
public final class MenuFrame<S> {

  private final MenuLayout layout;
  private final List<MenuSlot<S>> slots;

  public MenuFrame(MenuLayout layout, List<MenuSlot<S>> slots) {
    this.layout = Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(slots, "slots");

    if (slots.size() != layout.size()) {
      throw new IllegalArgumentException(
          "Frame slot count must match the layout size: " + slots.size() + " != " + layout.size());
    }

    this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
  }

  public MenuLayout layout() {
    return this.layout;
  }

  public int size() {
    return this.slots.size();
  }

  public Optional<MenuSlot<S>> slot(int slot) {
    return Optional.ofNullable(this.slotOrNull(slot));
  }

  public boolean occupied(int slot) {
    return this.slotOrNull(slot) != null;
  }

  public MenuSlot<S> slotOrNull(int slot) {
    this.layout.checkSlot(slot);
    return this.slots.get(slot);
  }
}
