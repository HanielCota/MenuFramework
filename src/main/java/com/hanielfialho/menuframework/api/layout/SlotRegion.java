package com.hanielfialho.menuframework.api.layout;

import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Immutable ordered collection of unique menu slots.
 *
 * <p>Slot order is significant and is preserved by every factory. This makes a region suitable for
 * grids, pagination and reusable components that map data by position.
 */
public final class SlotRegion implements Iterable<Integer> {

  private final List<Integer> slots;

  private SlotRegion(List<Integer> slots) {
    this.slots = List.copyOf(slots);
  }

  /**
   * Creates a region after validating every slot against a layout.
   *
   * @param layout owning layout
   * @param slots ordered slots
   * @return immutable region
   * @throws NullPointerException if an argument is {@code null}
   * @throws IllegalArgumentException if no slot is supplied or a slot is duplicated
   * @throws IndexOutOfBoundsException if a slot is outside the layout
   */
  public static SlotRegion of(MenuLayout layout, int... slots) {
    Objects.requireNonNull(slots, "slots");

    List<Integer> values = new ArrayList<>(slots.length);
    for (int slot : slots) {
      values.add(slot);
    }

    return copyOf(layout, values);
  }

  /**
   * Creates a region from an ordered collection.
   *
   * @param layout owning layout
   * @param slots ordered slots
   * @return immutable region
   * @throws NullPointerException if an argument or collection entry is {@code null}
   * @throws IllegalArgumentException if the collection is empty or contains duplicates
   * @throws IndexOutOfBoundsException if a slot is outside the layout
   */
  public static SlotRegion copyOf(MenuLayout layout, Collection<Integer> slots) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(slots, "slots");

    if (slots.isEmpty()) {
      throw new IllegalArgumentException("A slot region cannot be empty");
    }

    LinkedHashSet<Integer> unique = new LinkedHashSet<>();

    for (Integer value : slots) {
      int slot = Objects.requireNonNull(value, "slots contains null");
      layout.checkSlot(slot);

      if (!unique.add(slot)) {
        throw new IllegalArgumentException("Duplicate region slot: " + slot);
      }
    }

    return new SlotRegion(new ArrayList<>(unique));
  }

  /**
   * Returns the number of slots in this region.
   *
   * @return positive slot count
   */
  public int size() {
    return this.slots.size();
  }

  /**
   * Returns the raw menu slot at a region-relative position.
   *
   * @param position zero-based position inside the region
   * @return raw menu slot
   * @throws IndexOutOfBoundsException if the position is invalid
   */
  public int slot(int position) {
    return this.slots.get(position);
  }

  /**
   * Returns whether this region contains a raw slot.
   *
   * @param slot raw menu slot
   * @return containment flag
   */
  public boolean contains(int slot) {
    return this.slots.contains(slot);
  }

  /**
   * Returns the slots in deterministic order.
   *
   * @return immutable list
   */
  public List<Integer> slots() {
    return this.slots;
  }

  /**
   * Returns the slots as a primitive array.
   *
   * @return new array in region order
   */
  public int[] toArray() {
    return this.slots.stream().mapToInt(Integer::intValue).toArray();
  }

  /**
   * Returns a primitive stream in region order.
   *
   * @return ordered slot stream
   */
  public IntStream stream() {
    return this.slots.stream().mapToInt(Integer::intValue);
  }

  /**
   * Invokes a primitive consumer for every raw slot.
   *
   * @param consumer slot consumer
   */
  public void forEachSlot(IntConsumer consumer) {
    Objects.requireNonNull(consumer, "consumer");
    this.slots.forEach(consumer::accept);
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Integer> iterator() {
    return this.slots.iterator();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object) {
    return this == object || (object instanceof SlotRegion other && this.slots.equals(other.slots));
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return this.slots.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "SlotRegion" + this.slots;
  }
}
