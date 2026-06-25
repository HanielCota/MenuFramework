package com.hanielfialho.menuframework.api.pagination.async;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import com.hanielfialho.menuframework.api.pagination.PaginationLayout;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import com.hanielfialho.menuframework.api.theme.MenuThemeKey;
import com.hanielfialho.menuframework.api.theme.StandardMenuThemeKeys;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * High-level component for asynchronous pagination with internal state management.
 *
 * <p>This component hides {@link AsyncPageState} and the {@link PageStateAdapter}. The caller only
 * supplies the layout, the asynchronous source, and how each entry is rendered.
 *
 * @param <T> entry type
 */
public final class AsyncPaginationComponent<T> implements Menu<AsyncPaginationComponent.State<T>> {

  private final PaginationLayout layout;
  private final AsyncPaginator<T> paginator;
  private final Function<? super T, ItemStack> entryRenderer;
  private final BiConsumer<? super T, MenuInteraction<State<T>>> entryHandler;
  private final Function<? super PageSlice<T>, ItemStack> indicatorRenderer;

  private AsyncPaginationComponent(Builder<T> builder) {
    this.layout = builder.layout;
    this.paginator = builder.paginator;
    this.entryRenderer = builder.entryRenderer;
    this.entryHandler = builder.entryHandler;
    this.indicatorRenderer = builder.indicatorRenderer;
  }

  /**
   * Starts a builder for an asynchronous pagination component.
   *
   * @param key logical task key
   * @param layout pagination layout
   * @param source asynchronous source
   * @param <T> entry type
   * @return builder
   */
  public static <T> Builder<T> builder(String key, PaginationLayout layout, PageSource<T> source) {
    return new Builder<>(key, layout, source);
  }

  /**
   * Creates an initial state for this component.
   *
   * @param cursor initial cursor
   * @param layout pagination layout
   * @param <T> entry type
   * @return initial state
   */
  public static <T> State<T> initial(PageCursor cursor, PaginationLayout layout) {
    return new State<>(AsyncPageState.initial(layout.request(cursor)));
  }

  @Override
  public MenuLayout layout() {
    return this.layout.menuLayout();
  }

  @Override
  public Component title(MenuRenderContext<State<T>> context) {
    return Component.text("Async Pagination", NamedTextColor.GOLD);
  }

  @Override
  public void onOpen(MenuOpenContext<State<T>> context) {
    this.paginator.load(
        context,
        new PageStateAdapter<>() {
          @Override
          public AsyncPageState<T> pageState(State<T> state) {
            return state.pageState();
          }

          @Override
          public State<T> withPageState(State<T> state, AsyncPageState<T> pageState) {
            return new State<>(pageState);
          }
        },
        context.state().pageState().request());
  }

  @Override
  public void render(MenuRenderContext<State<T>> context, MenuCanvas<State<T>> canvas) {
    AsyncPageState<T> pageState = context.state().pageState();

    switch (pageState.status()) {
      case LOADING -> this.renderLoading(context, canvas);
      case READY -> this.renderReady(context, canvas, pageState.requirePage());
      case ERROR -> this.renderError(context, canvas);
    }
  }

  private void renderLoading(MenuRenderContext<State<T>> context, MenuCanvas<State<T>> canvas) {
    this.layout.contentSlots().forEach(canvas::empty);
    canvas.item(
        this.layout.previousSlot(), themeItem(context, StandardMenuThemeKeys.PREVIOUS_DISABLED));
    canvas.item(this.layout.nextSlot(), themeItem(context, StandardMenuThemeKeys.NEXT_DISABLED));

    this.layout
        .indicatorSlot()
        .ifPresent(
            slot -> canvas.component(context, MenuComponents.loadingIndicator(slotName(slot))));
  }

  private void renderReady(
      MenuRenderContext<State<T>> context, MenuCanvas<State<T>> canvas, PageSlice<T> page) {
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
            this.pageHandler(page.cursor().previous())));

    canvas.component(
        context,
        MenuComponents.nextPageButton(
            slotName(this.layout.nextSlot()),
            ignored -> page.hasNext(),
            this.pageHandler(page.cursor().next())));

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

  private void renderError(MenuRenderContext<State<T>> context, MenuCanvas<State<T>> canvas) {
    this.layout.contentSlots().forEach(canvas::empty);
    canvas.item(
        this.layout.previousSlot(), themeItem(context, StandardMenuThemeKeys.PREVIOUS_DISABLED));
    canvas.item(this.layout.nextSlot(), themeItem(context, StandardMenuThemeKeys.NEXT_DISABLED));

    this.layout
        .indicatorSlot()
        .ifPresent(
            slot ->
                canvas.component(
                    context, MenuComponents.retryButton(slotName(slot), this.retryHandler())));
  }

  private MenuClickHandler<State<T>> pageHandler(PageCursor cursor) {
    return interaction ->
        this.paginator.load(
            interaction,
            new PageStateAdapter<>() {
              @Override
              public AsyncPageState<T> pageState(State<T> state) {
                return state.pageState();
              }

              @Override
              public State<T> withPageState(State<T> state, AsyncPageState<T> pageState) {
                return new State<>(pageState);
              }
            },
            this.layout,
            cursor);
  }

  private MenuClickHandler<State<T>> retryHandler() {
    return interaction ->
        this.paginator.reload(
            interaction,
            new PageStateAdapter<>() {
              @Override
              public AsyncPageState<T> pageState(State<T> state) {
                return state.pageState();
              }

              @Override
              public State<T> withPageState(State<T> state, AsyncPageState<T> pageState) {
                return new State<>(pageState);
              }
            });
  }

  private static ItemStack themeItem(MenuRenderContext<?> context, MenuThemeKey key) {
    return MenuTheme.defaults().item(key, context);
  }

  private static String slotName(int slot) {
    return String.valueOf(slot);
  }

  private static <T> ItemStack defaultIndicator(PageSlice<T> page) {
    ItemStack indicator = ItemStack.of(Material.PAPER);
    indicator.editMeta(
        meta -> meta.displayName(Component.text("Pagina " + page.number(), NamedTextColor.AQUA)));
    return indicator;
  }

  /** Mutable builder for {@link AsyncPaginationComponent}. */
  public static final class Builder<T> {

    private final PaginationLayout layout;
    private final AsyncPaginator<T> paginator;
    private Function<? super T, ItemStack> entryRenderer;
    private BiConsumer<? super T, MenuInteraction<State<T>>> entryHandler =
        (entry, interaction) -> {};
    private Function<? super PageSlice<T>, ItemStack> indicatorRenderer;

    private Builder(String key, PaginationLayout layout, PageSource<T> source) {
      this.layout = Objects.requireNonNull(layout, "layout");
      this.paginator = AsyncPaginator.create(key, Objects.requireNonNull(source, "source"));
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
    public Builder<T> onSelect(BiConsumer<? super T, MenuInteraction<State<T>>> handler) {
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
    public AsyncPaginationComponent<T> build() {
      Objects.requireNonNull(this.entryRenderer, "entryRenderer must be configured");
      return new AsyncPaginationComponent<>(this);
    }
  }

  /**
   * Internal state wrapper for the component.
   *
   * @param <T> entry type
   */
  public record State<T>(AsyncPageState<T> pageState) {

    /**
     * Validates the state.
     *
     * @throws NullPointerException if {@code pageState} is null
     */
    public State {
      Objects.requireNonNull(pageState, "pageState");
    }
  }
}
