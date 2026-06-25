package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.api.layout.SlotPattern;
import com.hanielfialho.menuframework.api.layout.SlotRegion;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import org.bukkit.event.inventory.InventoryType;

/**
 * Immutable structural description of a menu inventory.
 *
 * <p>This version supports chest inventories containing one through six rows. A layout may also
 * expose named slots and named ordered regions. Named metadata has no runtime cost after layout
 * construction and does not change Bukkit's inventory shape.
 */
public final class MenuLayout {

  /** Number of columns in a chest inventory. */
  public static final int CHEST_COLUMNS = 9;

  /** Minimum supported chest row count. */
  public static final int MIN_CHEST_ROWS = 1;

  /** Maximum supported chest row count. */
  public static final int MAX_CHEST_ROWS = 6;

  private final InventoryType inventoryType;
  private final int rows;
  private final int size;
  private final Map<String, Integer> namedSlots;
  private final Map<String, SlotRegion> namedRegions;

  private MenuLayout(
      InventoryType inventoryType,
      int rows,
      int size,
      Map<String, Integer> namedSlots,
      Map<String, SlotRegion> namedRegions) {
    this.inventoryType = Objects.requireNonNull(inventoryType, "inventoryType");
    this.rows = rows;
    this.size = size;
    this.namedSlots = immutableLinkedMap(namedSlots);
    this.namedRegions = immutableLinkedMap(namedRegions);
  }

  /**
   * Creates a validated chest layout without named metadata.
   *
   * @param rows row count between {@link #MIN_CHEST_ROWS} and {@link #MAX_CHEST_ROWS}, inclusive
   * @return immutable layout
   * @throws IllegalArgumentException if {@code rows} is outside the range
   */
  public static MenuLayout chest(int rows) {
    validateChestRows(rows);
    return new MenuLayout(InventoryType.CHEST, rows, rows * CHEST_COLUMNS, Map.of(), Map.of());
  }

  /**
   * Creates a named-layout builder for a chest inventory.
   *
   * @param rows validated chest row count
   * @return new builder
   */
  public static Builder chestBuilder(int rows) {
    return chest(rows).toBuilder();
  }

  /**
   * Creates a standard pagination layout with previous, indicator and next controls.
   *
   * @param rows row count, normally 4 through 6
   * @return layout with slots {@code previous}, {@code indicator}, {@code next}
   */
  public static MenuLayout standardPage(int rows) {
    int lastRow = rows - 1;
    return chestBuilder(rows)
        .slot("previous", lastRow, 0)
        .slot("indicator", lastRow, 4)
        .slot("next", lastRow, 8)
        .build();
  }

  /**
   * Creates a confirmation layout with message, confirm and cancel slots.
   *
   * @return layout with slots {@code message}, {@code confirm}, {@code cancel}
   */
  public static MenuLayout confirmation() {
    return chestBuilder(3).slot("confirm", 1, 2).slot("message", 1, 4).slot("cancel", 1, 6).build();
  }

  /**
   * Creates a builder populated with this layout's named metadata.
   *
   * @return independent builder
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Returns the Bukkit inventory type.
   *
   * @return {@link InventoryType#CHEST}
   */
  public InventoryType inventoryType() {
    return this.inventoryType;
  }

  /**
   * Returns the number of rows.
   *
   * @return row count
   */
  public int rows() {
    return this.rows;
  }

  /**
   * Returns the total number of top-inventory slots.
   *
   * @return inventory size
   */
  public int size() {
    return this.size;
  }

  /**
   * Converts zero-based row and column coordinates into a linear slot.
   *
   * @param row zero-based row
   * @param column zero-based column
   * @return linear slot index
   * @throws IndexOutOfBoundsException if either coordinate is invalid
   */
  public int slot(int row, int column) {
    if (row < 0 || row >= this.rows) {
      throw new IndexOutOfBoundsException(
          "Row must be between 0 and " + (this.rows - 1) + ": " + row);
    }

    if (column < 0 || column >= CHEST_COLUMNS) {
      throw new IndexOutOfBoundsException(
          "Column must be between 0 and " + (CHEST_COLUMNS - 1) + ": " + column);
    }

    return (row * CHEST_COLUMNS) + column;
  }

  /**
   * Resolves a required named slot.
   *
   * @param name exact slot name
   * @return raw menu slot
   * @throws IllegalArgumentException if the name is unknown
   */
  public int slot(String name) {
    String checkedName = validateName(name);
    Integer slot = this.namedSlots.get(checkedName);

    if (slot == null) {
      throw new IllegalArgumentException("Unknown named menu slot: " + checkedName);
    }

    return slot;
  }

  /**
   * Finds a named slot.
   *
   * @param name exact slot name
   * @return optional raw slot
   */
  public OptionalInt findSlot(String name) {
    String checkedName = validateName(name);
    Integer slot = this.namedSlots.get(checkedName);
    return slot == null ? OptionalInt.empty() : OptionalInt.of(slot);
  }

  /**
   * Returns whether a named slot exists.
   *
   * @param name exact slot name
   * @return existence flag
   */
  public boolean hasSlot(String name) {
    return this.namedSlots.containsKey(validateName(name));
  }

  /**
   * Resolves a required named region.
   *
   * @param name exact region name
   * @return immutable ordered region
   * @throws IllegalArgumentException if the name is unknown
   */
  public SlotRegion region(String name) {
    String checkedName = validateName(name);
    SlotRegion region = this.namedRegions.get(checkedName);

    if (region == null) {
      throw new IllegalArgumentException("Unknown named menu region: " + checkedName);
    }

    return region;
  }

  /**
   * Finds a named region.
   *
   * @param name exact region name
   * @return optional immutable region
   */
  public Optional<SlotRegion> findRegion(String name) {
    return Optional.ofNullable(this.namedRegions.get(validateName(name)));
  }

  /**
   * Returns whether a named region exists.
   *
   * @param name exact region name
   * @return existence flag
   */
  public boolean hasRegion(String name) {
    return this.namedRegions.containsKey(validateName(name));
  }

  /**
   * Returns all named slots in declaration order.
   *
   * @return immutable mapping
   */
  public Map<String, Integer> namedSlots() {
    return this.namedSlots;
  }

  /**
   * Returns all named regions in declaration order.
   *
   * @return immutable mapping
   */
  public Map<String, SlotRegion> namedRegions() {
    return this.namedRegions;
  }

  /**
   * Returns the zero-based row containing a slot.
   *
   * @param slot linear slot
   * @return zero-based row
   * @throws IndexOutOfBoundsException if {@code slot} is invalid
   */
  public int rowOf(int slot) {
    this.checkSlot(slot);
    return slot / CHEST_COLUMNS;
  }

  /**
   * Returns the zero-based column containing a slot.
   *
   * @param slot linear slot
   * @return zero-based column
   * @throws IndexOutOfBoundsException if {@code slot} is invalid
   */
  public int columnOf(int slot) {
    this.checkSlot(slot);
    return slot % CHEST_COLUMNS;
  }

  /**
   * Tests whether a slot belongs to this layout.
   *
   * @param slot linear slot
   * @return {@code true} when the slot is inside the inventory
   */
  public boolean contains(int slot) {
    return slot >= 0 && slot < this.size;
  }

  /**
   * Validates and returns a slot index.
   *
   * @param slot linear slot
   * @return the supplied slot
   * @throws IndexOutOfBoundsException if the slot does not belong to this layout
   */
  public int checkSlot(int slot) {
    if (!this.contains(slot)) {
      throw new IndexOutOfBoundsException(
          "Slot must be between 0 and " + (this.size - 1) + ": " + slot);
    }

    return slot;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof MenuLayout other)) {
      return false;
    }

    return this.rows == other.rows
        && this.size == other.size
        && this.inventoryType == other.inventoryType
        && this.namedSlots.equals(other.namedSlots)
        && this.namedRegions.equals(other.namedRegions);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(
        this.inventoryType, this.rows, this.size, this.namedSlots, this.namedRegions);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "MenuLayout{"
        + "inventoryType="
        + this.inventoryType
        + ", rows="
        + this.rows
        + ", size="
        + this.size
        + ", namedSlots="
        + this.namedSlots
        + ", namedRegions="
        + this.namedRegions.keySet()
        + '}';
  }

  private static void validateChestRows(int rows) {
    if (rows < MIN_CHEST_ROWS || rows > MAX_CHEST_ROWS) {
      throw new IllegalArgumentException(
          "Chest rows must be between " + MIN_CHEST_ROWS + " and " + MAX_CHEST_ROWS + ": " + rows);
    }
  }

  private static String validateName(String name) {
    String checkedName = Objects.requireNonNull(name, "name");

    if (checkedName.isBlank()) {
      throw new IllegalArgumentException("Layout name cannot be blank");
    }

    if (!checkedName.equals(checkedName.strip())) {
      throw new IllegalArgumentException(
          "Layout name cannot contain leading or trailing whitespace: '" + checkedName + "'");
    }

    return checkedName;
  }

  private static <K, V> Map<K, V> immutableLinkedMap(Map<K, V> values) {
    Objects.requireNonNull(values, "values");
    return Collections.unmodifiableMap(new LinkedHashMap<>(values));
  }

  /** Validating builder for named slots and regions. */
  public static final class Builder {

    private final MenuLayout structuralLayout;
    private final LinkedHashMap<String, Integer> namedSlots;
    private final LinkedHashMap<String, SlotRegion> namedRegions;
    private final LinkedHashMap<Integer, String> slotOwners;
    private final Set<String> names;

    private Builder(MenuLayout source) {
      this.structuralLayout =
          new MenuLayout(source.inventoryType, source.rows, source.size, Map.of(), Map.of());
      this.namedSlots = new LinkedHashMap<>(source.namedSlots);
      this.namedRegions = new LinkedHashMap<>(source.namedRegions);
      this.slotOwners = new LinkedHashMap<>();
      this.names = new LinkedHashSet<>();

      for (Map.Entry<String, Integer> entry : this.namedSlots.entrySet()) {
        this.names.add(entry.getKey());
        this.slotOwners.put(entry.getValue(), entry.getKey());
      }
      this.names.addAll(this.namedRegions.keySet());
    }

    /**
     * Declares a named raw slot.
     *
     * <p>Two names cannot identify the same raw slot. A named slot may intentionally belong to one
     * or more named regions.
     *
     * @param name unique name
     * @param slot raw slot
     * @return this builder
     */
    public Builder slot(String name, int slot) {
      String checkedName = validateName(name);
      int checkedSlot = this.structuralLayout.checkSlot(slot);
      String existingOwner = this.slotOwners.get(checkedSlot);

      if (existingOwner != null) {
        throw new IllegalArgumentException(
            "Named slot '"
                + checkedName
                + "' overlaps named slot '"
                + existingOwner
                + "' at raw slot "
                + checkedSlot);
      }

      this.reserveName(checkedName);
      this.slotOwners.put(checkedSlot, checkedName);
      this.namedSlots.put(checkedName, checkedSlot);
      return this;
    }

    /**
     * Declares a named slot using zero-based coordinates.
     *
     * @param name unique name
     * @param row zero-based row
     * @param column zero-based column
     * @return this builder
     */
    public Builder slot(String name, int row, int column) {
      return this.slot(name, this.structuralLayout.slot(row, column));
    }

    /**
     * Declares a named region resolved from a pattern.
     *
     * <p>Named regions are metadata views and may overlap other regions or named slots. Actual
     * duplicate assignments are still rejected by {@link MenuCanvas} during rendering.
     *
     * @param name unique name
     * @param pattern region pattern
     * @return this builder
     */
    public Builder region(String name, SlotPattern pattern) {
      Objects.requireNonNull(pattern, "pattern");
      return this.region(name, pattern.resolve(this.structuralLayout));
    }

    /**
     * Declares a named region.
     *
     * @param name unique name
     * @param region ordered region whose slots belong to this layout
     * @return this builder
     */
    public Builder region(String name, SlotRegion region) {
      String checkedName = validateName(name);
      SlotRegion checkedRegion =
          SlotRegion.copyOf(
              this.structuralLayout, Objects.requireNonNull(region, "region").slots());
      this.reserveName(checkedName);
      this.namedRegions.put(checkedName, checkedRegion);
      return this;
    }

    /**
     * Declares a named region from explicit raw slots.
     *
     * @param name unique name
     * @param slots ordered raw slots
     * @return this builder
     */
    public Builder region(String name, int... slots) {
      return this.region(name, SlotRegion.of(this.structuralLayout, slots));
    }

    /**
     * Builds an immutable layout.
     *
     * @return immutable layout
     */
    public MenuLayout build() {
      return new MenuLayout(
          this.structuralLayout.inventoryType,
          this.structuralLayout.rows,
          this.structuralLayout.size,
          this.namedSlots,
          this.namedRegions);
    }

    private String reserveName(String name) {
      String checkedName = validateName(name);

      if (!this.names.add(checkedName)) {
        throw new IllegalArgumentException(
            "A named slot or region already uses name: " + checkedName);
      }

      return checkedName;
    }
  }
}
