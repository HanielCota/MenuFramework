package com.hanielfialho.menuframework.api.pagination;

import com.hanielfialho.menuframework.api.MenuLayout;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntConsumer;
import org.jspecify.annotations.Nullable;

/**
 * Immutable mapping between page entries and menu slots.
 *
 * <p>The page size is defined exclusively by the number of content slots. Navigation controls
 * cannot overlap each other or a content slot.
 */
public final class PaginationLayout {

  private final MenuLayout menuLayout;
  private final List<Integer> contentSlots;
  private final int previousSlot;
  private final int nextSlot;
  private final @Nullable Integer indicatorSlot;

  private PaginationLayout(
      MenuLayout menuLayout,
      List<Integer> contentSlots,
      int previousSlot,
      int nextSlot,
      @Nullable Integer indicatorSlot) {
    this.menuLayout = menuLayout;
    this.contentSlots = contentSlots;
    this.previousSlot = previousSlot;
    this.nextSlot = nextSlot;
    this.indicatorSlot = indicatorSlot;
  }

  /**
   * Creates a builder associated with a structural menu layout.
   *
   * @param menuLayout non-null menu layout
   * @return a new builder
   */
  public static Builder builder(MenuLayout menuLayout) {
    return new Builder(menuLayout);
  }

  /**
   * Returns the associated structural menu layout.
   *
   * @return menu layout
   */
  public MenuLayout menuLayout() {
    return this.menuLayout;
  }

  /**
   * Returns content slots in fill order.
   *
   * @return immutable slot list
   */
  public List<Integer> contentSlots() {
    return this.contentSlots;
  }

  /**
   * Returns the capacity of one page.
   *
   * @return number of content slots
   */
  public int pageSize() {
    return this.contentSlots.size();
  }

  /**
   * Returns the previous-page control slot.
   *
   * @return validated slot
   */
  public int previousSlot() {
    return this.previousSlot;
  }

  /**
   * Returns the next-page control slot.
   *
   * @return validated slot
   */
  public int nextSlot() {
    return this.nextSlot;
  }

  /**
   * Returns the optional page-indicator slot.
   *
   * @return optional slot
   */
  public OptionalInt indicatorSlot() {
    return this.indicatorSlot == null ? OptionalInt.empty() : OptionalInt.of(this.indicatorSlot);
  }

  /**
   * Returns a content slot by its zero-based page position.
   *
   * @param index zero-based position in the page
   * @return corresponding menu slot
   * @throws IndexOutOfBoundsException if {@code index} is invalid
   */
  public int contentSlot(int index) {
    return this.contentSlots.get(index);
  }

  /**
   * Creates a request whose page size matches this layout.
   *
   * @param cursor non-null page cursor
   * @return compatible request
   */
  public PageRequest request(PageCursor cursor) {
    return new PageRequest(cursor, this.pageSize());
  }

  /**
   * Iterates over page entries and reports each corresponding menu slot.
   *
   * @param page page whose size matches this layout
   * @param consumer non-null entry consumer
   * @param <T> entry type
   * @throws IllegalArgumentException if the page size does not match
   */
  public <T> void forEachEntry(PageSlice<T> page, PageEntryConsumer<? super T> consumer) {
    Objects.requireNonNull(consumer, "consumer");
    this.validatePage(page);

    long offset = page.request().offset();
    List<T> entries = page.entries();

    for (int index = 0; index < entries.size(); index++) {
      consumer.accept(this.contentSlots.get(index), entries.get(index), index, offset + index);
    }
  }

  /**
   * Iterates over content slots not occupied by entries in the page.
   *
   * <p>A common use is passing {@code canvas::empty} so the unused content area remains empty even
   * when the menu has a background.
   *
   * @param page page whose size matches this layout
   * @param consumer non-null slot consumer
   * @throws IllegalArgumentException if the page size does not match
   */
  public void forEachUnusedSlot(PageSlice<?> page, IntConsumer consumer) {
    Objects.requireNonNull(consumer, "consumer");
    this.validatePage(page);

    for (int index = page.entries().size(); index < this.contentSlots.size(); index++) {
      consumer.accept(this.contentSlots.get(index));
    }
  }

  private void validatePage(PageSlice<?> page) {
    Objects.requireNonNull(page, "page");

    if (page.pageSize() != this.pageSize()) {
      throw new IllegalArgumentException(
          "Page size does not match the pagination layout: "
              + page.pageSize()
              + " != "
              + this.pageSize());
    }
  }

  /**
   * Validating builder for an immutable {@link PaginationLayout}.
   *
   * <p>The builder preserves content-slot insertion order and is not thread-safe.
   */
  public static final class Builder {

    private final MenuLayout menuLayout;
    private final LinkedHashSet<Integer> contentSlots = new LinkedHashSet<>();

    private @Nullable Integer previousSlot;
    private @Nullable Integer nextSlot;
    private @Nullable Integer indicatorSlot;

    private Builder(MenuLayout menuLayout) {
      this.menuLayout = Objects.requireNonNull(menuLayout, "menuLayout");
    }

    /**
     * Appends content slots in the supplied order.
     *
     * @param slots valid, non-duplicate slots
     * @return this builder
     */
    public Builder contentSlots(int... slots) {
      Objects.requireNonNull(slots, "slots");

      for (int slot : slots) {
        this.addContentSlot(slot);
      }

      return this;
    }

    /**
     * Appends an inclusive rectangular area in row-major order.
     *
     * @param firstRow first zero-based row
     * @param firstColumn first zero-based column
     * @param lastRow last zero-based row
     * @param lastColumn last zero-based column
     * @return this builder
     */
    public Builder contentArea(int firstRow, int firstColumn, int lastRow, int lastColumn) {
      if (firstRow > lastRow) {
        throw new IllegalArgumentException("firstRow cannot be greater than lastRow");
      }

      if (firstColumn > lastColumn) {
        throw new IllegalArgumentException("firstColumn cannot be greater than lastColumn");
      }

      for (int row = firstRow; row <= lastRow; row++) {
        for (int column = firstColumn; column <= lastColumn; column++) {
          this.addContentSlot(this.menuLayout.slot(row, column));
        }
      }

      return this;
    }

    /**
     * Sets the previous-page control slot.
     *
     * @param slot linear menu slot
     * @return this builder
     */
    public Builder previousSlot(int slot) {
      if (this.previousSlot != null) {
        throw new IllegalStateException("previousSlot has already been configured");
      }

      this.previousSlot = this.menuLayout.checkSlot(slot);
      return this;
    }

    /**
     * Sets the previous-page control using coordinates.
     *
     * @param row zero-based row
     * @param column zero-based column
     * @return this builder
     */
    public Builder previousSlot(int row, int column) {
      return this.previousSlot(this.menuLayout.slot(row, column));
    }

    /**
     * Sets the next-page control slot.
     *
     * @param slot linear menu slot
     * @return this builder
     */
    public Builder nextSlot(int slot) {
      if (this.nextSlot != null) {
        throw new IllegalStateException("nextSlot has already been configured");
      }

      this.nextSlot = this.menuLayout.checkSlot(slot);
      return this;
    }

    /**
     * Sets the next-page control using coordinates.
     *
     * @param row zero-based row
     * @param column zero-based column
     * @return this builder
     */
    public Builder nextSlot(int row, int column) {
      return this.nextSlot(this.menuLayout.slot(row, column));
    }

    /**
     * Sets an optional page-indicator slot.
     *
     * @param slot linear menu slot
     * @return this builder
     */
    public Builder indicatorSlot(int slot) {
      if (this.indicatorSlot != null) {
        throw new IllegalStateException("indicatorSlot has already been configured");
      }

      this.indicatorSlot = this.menuLayout.checkSlot(slot);
      return this;
    }

    /**
     * Sets the optional page indicator using coordinates.
     *
     * @param row zero-based row
     * @param column zero-based column
     * @return this builder
     */
    public Builder indicatorSlot(int row, int column) {
      return this.indicatorSlot(this.menuLayout.slot(row, column));
    }

    /**
     * Validates required controls and slot overlap, then builds the layout.
     *
     * @return immutable pagination layout
     * @throws IllegalStateException if required controls are missing or any control overlaps
     *     another configured slot
     */
    public PaginationLayout build() {
      if (this.contentSlots.isEmpty()) {
        throw new IllegalStateException("At least one content slot is required");
      }

      if (this.previousSlot == null) {
        throw new IllegalStateException("previousSlot must be configured");
      }

      if (this.nextSlot == null) {
        throw new IllegalStateException("nextSlot must be configured");
      }

      Set<Integer> controls = new HashSet<>();

      this.validateControl("previousSlot", this.previousSlot, controls);
      this.validateControl("nextSlot", this.nextSlot, controls);

      if (this.indicatorSlot != null) {
        this.validateControl("indicatorSlot", this.indicatorSlot, controls);
      }

      return new PaginationLayout(
          this.menuLayout,
          List.copyOf(this.contentSlots),
          this.previousSlot,
          this.nextSlot,
          this.indicatorSlot);
    }

    private void addContentSlot(int slot) {
      this.menuLayout.checkSlot(slot);

      if (!this.contentSlots.add(slot)) {
        throw new IllegalArgumentException("Duplicate content slot: " + slot);
      }
    }

    private void validateControl(String name, int slot, Set<Integer> controls) {
      if (this.contentSlots.contains(slot)) {
        throw new IllegalStateException(name + " overlaps a content slot: " + slot);
      }

      if (!controls.add(slot)) {
        throw new IllegalStateException(name + " overlaps another control: " + slot);
      }
    }
  }
}
