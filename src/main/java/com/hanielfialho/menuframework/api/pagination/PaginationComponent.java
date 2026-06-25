package com.hanielfialho.menuframework.api.pagination;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * High-level synchronous pagination component with internal cursor state.
 *
 * <p>The caller supplies the collection and how entries are rendered; the component manages the
 * {@link PageCursor} and navigation controls.
 *
 * @param <T> entry type
 */
public final class PaginationComponent<T> implements Menu<PaginationComponent.State> {

  private final String title;
  private final PaginationLayout layout;
  private final Paginator<T> paginator;
  private final Function<? super T, ItemStack> entryRenderer;
  private final BiConsumer<? super T, MenuInteraction<State>> entryHandler;
  private final Function<? super PageSlice<T>, ItemStack> indicatorRenderer;

  private PaginationComponent(Builder<T> builder, PaginationLayout layout) {
    this.title = builder.title;
    this.layout = layout;
    this.paginator = Paginator.copyOf(builder.items);
    this.entryRenderer = builder.entryRenderer;
    this.entryHandler = builder.entryHandler;
    this.indicatorRenderer = builder.indicatorRenderer;
  }

  /**
   * Starts a builder for a synchronous pagination menu.
   *
   * @param title menu title
   * @param rows number of rows, from 1 to 6
   * @param <T> entry type
   * @return builder
   */
  public static <T> Builder<T> builder(String title, int rows) {
    return new Builder<>(title, rows);
  }

  /**
   * Creates an initial state pointing to the first page.
   *
   * @return initial state
   */
  public static State initial() {
    return new State(PageCursor.FIRST);
  }

  @Override
  public MenuLayout layout() {
    return this.layout.menuLayout();
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text(this.title, NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    PageSlice<T> page = this.paginator.page(this.layout.request(context.state().cursor()));

    this.layout.forEachEntry(
        page,
        (slot, entry, indexInPage, absoluteIndex) ->
            canvas.button(
                slot,
                this.entryRenderer.apply(entry),
                interaction -> this.entryHandler.accept(entry, interaction)));

    this.layout.forEachUnusedSlot(page, canvas::empty);

    canvas.component(
        context,
        MenuComponents.previousPageButton(
            slotName(this.layout.previousSlot()),
            ignored -> page.hasPrevious(),
            interaction ->
                interaction.updateState(state -> state.withCursor(page.cursor().previous()))));

    canvas.component(
        context,
        MenuComponents.nextPageButton(
            slotName(this.layout.nextSlot()),
            ignored -> page.hasNext(),
            interaction ->
                interaction.updateState(state -> state.withCursor(page.cursor().next()))));

    this.layout
        .indicatorSlot()
        .ifPresent(
            slot ->
                canvas.item(
                    slot,
                    this.indicatorRenderer == null
                        ? defaultIndicator(page)
                        : this.indicatorRenderer.apply(page)));
  }

  private static String slotName(int slot) {
    return String.valueOf(slot);
  }

  private static <T> ItemStack defaultIndicator(PageSlice<T> page) {
    ItemStack indicator = ItemStack.of(Material.PAPER);
    indicator.editMeta(
        meta -> {
          long total = page.totalElements().orElse(-1L);
          String suffix = total >= 0L ? " / " + page.totalPages().orElseThrow() : "";
          meta.displayName(Component.text("Pagina " + page.number() + suffix, NamedTextColor.AQUA));
        });
    return indicator;
  }

  /** Mutable builder for {@link PaginationComponent}. */
  public static final class Builder<T> {

    private final String title;
    private final int rows;
    private MenuLayout menuLayout;
    private int previousRow = -1;
    private int previousColumn = -1;
    private int indicatorRow = -1;
    private int indicatorColumn = -1;
    private int nextRow = -1;
    private int nextColumn = -1;
    private int contentFirstRow = -1;
    private int contentFirstColumn = -1;
    private int contentLastRow = -1;
    private int contentLastColumn = -1;
    private Collection<? extends T> items = java.util.List.of();
    private Function<? super T, ItemStack> entryRenderer;
    private BiConsumer<? super T, MenuInteraction<State>> entryHandler = (entry, interaction) -> {};
    private Function<? super PageSlice<T>, ItemStack> indicatorRenderer;

    private Builder(String title, int rows) {
      if (rows < 1 || rows > 6) {
        throw new IllegalArgumentException("rows must be between 1 and 6: " + rows);
      }
      this.title = Objects.requireNonNull(title, "title");
      this.rows = rows;
      this.menuLayout = MenuLayout.chest(rows);
    }

    /**
     * Sets a custom menu layout with named slots for previous, next and indicator.
     *
     * @param layout custom layout
     * @return this builder
     */
    public Builder<T> layout(MenuLayout layout) {
      this.menuLayout = Objects.requireNonNull(layout, "layout");
      return this;
    }

    /**
     * Defines the content area using coordinates.
     *
     * @param firstRow first row
     * @param firstColumn first column
     * @param lastRow last row
     * @param lastColumn last column
     * @return this builder
     */
    public Builder<T> contentArea(int firstRow, int firstColumn, int lastRow, int lastColumn) {
      this.contentFirstRow = firstRow;
      this.contentFirstColumn = firstColumn;
      this.contentLastRow = lastRow;
      this.contentLastColumn = lastColumn;
      return this;
    }

    /**
     * Defines the navigation-control positions using coordinates.
     *
     * @param previousRow previous-button row
     * @param previousColumn previous-button column
     * @param indicatorRow indicator row
     * @param indicatorColumn indicator column
     * @param nextRow next-button row
     * @param nextColumn next-button column
     * @return this builder
     */
    public Builder<T> controls(
        int previousRow,
        int previousColumn,
        int indicatorRow,
        int indicatorColumn,
        int nextRow,
        int nextColumn) {
      this.previousRow = previousRow;
      this.previousColumn = previousColumn;
      this.indicatorRow = indicatorRow;
      this.indicatorColumn = indicatorColumn;
      this.nextRow = nextRow;
      this.nextColumn = nextColumn;
      return this;
    }

    /**
     * Sets the paginated items.
     *
     * @param items items
     * @return this builder
     */
    public Builder<T> items(Collection<? extends T> items) {
      this.items = Objects.requireNonNull(items, "items");
      return this;
    }

    /**
     * Sets the renderer that converts an entry into an item.
     *
     * @param renderer entry renderer
     * @return this builder
     */
    public Builder<T> entryRenderer(Function<? super T, ItemStack> renderer) {
      this.entryRenderer = Objects.requireNonNull(renderer, "renderer");
      return this;
    }

    /**
     * Sets the handler invoked when an entry button is clicked.
     *
     * @param handler entry click handler
     * @return this builder
     */
    public Builder<T> onSelect(BiConsumer<? super T, MenuInteraction<State>> handler) {
      this.entryHandler = Objects.requireNonNull(handler, "handler");
      return this;
    }

    /**
     * Sets a custom page indicator icon.
     *
     * @param renderer indicator renderer
     * @return this builder
     */
    public Builder<T> indicatorRenderer(Function<? super PageSlice<T>, ItemStack> renderer) {
      this.indicatorRenderer = Objects.requireNonNull(renderer, "renderer");
      return this;
    }

    /**
     * Builds the component.
     *
     * @return component
     */
    public PaginationComponent<T> build() {
      Objects.requireNonNull(this.entryRenderer, "entryRenderer must be configured");

      MenuLayout layout = this.buildLayout();
      PaginationLayout pagination = this.buildPagination(layout);

      return new PaginationComponent<>(this, pagination);
    }

    private MenuLayout buildLayout() {
      if (this.previousRow < 0) {
        this.applyDefaultControls();
      }

      return MenuLayout.chestBuilder(this.rows)
          .slot("previous", this.previousRow, this.previousColumn)
          .slot("indicator", this.indicatorRow, this.indicatorColumn)
          .slot("next", this.nextRow, this.nextColumn)
          .build();
    }

    private PaginationLayout buildPagination(MenuLayout layout) {
      if (this.contentFirstRow < 0) {
        this.applyDefaultContentArea();
      }

      return PaginationLayout.builder(layout)
          .contentArea(
              this.contentFirstRow,
              this.contentFirstColumn,
              this.contentLastRow,
              this.contentLastColumn)
          .previousSlot("previous")
          .indicatorSlot("indicator")
          .nextSlot("next")
          .build();
    }

    private void applyDefaultControls() {
      this.previousRow = this.rows - 1;
      this.previousColumn = 0;
      this.indicatorRow = this.rows - 1;
      this.indicatorColumn = 4;
      this.nextRow = this.rows - 1;
      this.nextColumn = 8;
    }

    private void applyDefaultContentArea() {
      this.contentFirstRow = 1;
      this.contentFirstColumn = 1;
      this.contentLastRow = this.rows - 2;
      this.contentLastColumn = 7;
    }
  }

  /** Internal state wrapper for the component. */
  public record State(PageCursor cursor) {

    /**
     * Validates the state.
     *
     * @throws NullPointerException if {@code cursor} is null
     */
    public State {
      Objects.requireNonNull(cursor, "cursor");
    }

    State withCursor(PageCursor cursor) {
      return new State(cursor);
    }
  }
}
