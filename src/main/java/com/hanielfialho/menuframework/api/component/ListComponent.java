package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.layout.SlotRegion;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;

/**
 * Component that renders a static list of items into a named region.
 *
 * <p>Useful for small, non-paginated lists such as player statistics, warp points or help topics.
 *
 * @param <S> menu-state type
 * @param <T> entry type
 */
public final class ListComponent<S, T> implements MenuComponent<S> {

  private final String regionName;
  private final Function<? super MenuRenderContext<S>, ? extends List<? extends T>> entries;
  private final Function<? super T, ItemStack> entryRenderer;
  private final BiConsumer<? super T, MenuInteraction<S>> entryHandler;

  private ListComponent(Builder<S, T> builder) {
    this.regionName = builder.regionName;
    this.entries = builder.entries;
    this.entryRenderer = builder.entryRenderer;
    this.entryHandler = builder.entryHandler;
  }

  /**
   * Starts a builder for a list component.
   *
   * @param regionName named region that will contain the list
   * @param <S> menu-state type
   * @param <T> entry type
   * @return builder
   */
  public static <S, T> Builder<S, T> builder(String regionName) {
    return new Builder<>(regionName);
  }

  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    SlotRegion region = canvas.layout().region(this.regionName);
    int[] slots = region.toArray();
    List<? extends T> items = this.entries.apply(context);

    for (int index = 0; index < slots.length; index++) {
      if (index < items.size()) {
        T item = items.get(index);
        canvas.button(slots[index], this.entryRenderer.apply(item), this.handlerFor(item));
      } else {
        canvas.empty(slots[index]);
      }
    }
  }

  private MenuClickHandler<S> handlerFor(T item) {
    return interaction -> this.entryHandler.accept(item, interaction);
  }

  /** Mutable builder for {@link ListComponent}. */
  public static final class Builder<S, T> {

    private final String regionName;
    private Function<? super MenuRenderContext<S>, ? extends List<? extends T>> entries =
        context -> List.of();
    private Function<? super T, ItemStack> entryRenderer;
    private BiConsumer<? super T, MenuInteraction<S>> entryHandler = (entry, interaction) -> {};

    private Builder(String regionName) {
      this.regionName = Objects.requireNonNull(regionName, "regionName");
    }

    /**
     * Sets the entries resolver.
     *
     * @param entries entries resolver
     * @return this builder
     */
    public Builder<S, T> entries(
        Function<? super MenuRenderContext<S>, ? extends List<? extends T>> entries) {
      this.entries = Objects.requireNonNull(entries, "entries");
      return this;
    }

    /**
     * Sets a fixed list of entries.
     *
     * @param entries fixed entries
     * @return this builder
     */
    public Builder<S, T> entries(List<? extends T> entries) {
      this.entries = context -> Objects.requireNonNull(entries, "entries");
      return this;
    }

    /**
     * Sets the renderer that converts an entry into an item.
     *
     * @param renderer entry renderer
     * @return this builder
     */
    public Builder<S, T> entryRenderer(Function<? super T, ItemStack> renderer) {
      this.entryRenderer = Objects.requireNonNull(renderer, "renderer");
      return this;
    }

    /**
     * Sets the handler invoked when an entry button is clicked.
     *
     * @param handler entry click handler
     * @return this builder
     */
    public Builder<S, T> onSelect(BiConsumer<? super T, MenuInteraction<S>> handler) {
      this.entryHandler = Objects.requireNonNull(handler, "handler");
      return this;
    }

    /**
     * Builds the component.
     *
     * @return component
     */
    public ListComponent<S, T> build() {
      Objects.requireNonNull(this.entryRenderer, "entryRenderer must be configured");
      return new ListComponent<>(this);
    }
  }
}
