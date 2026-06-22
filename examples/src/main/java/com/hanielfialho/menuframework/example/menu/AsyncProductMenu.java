package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import com.hanielfialho.menuframework.api.pagination.PaginationLayout;
import com.hanielfialho.menuframework.api.pagination.async.AsyncPageState;
import com.hanielfialho.menuframework.api.pagination.async.AsyncPaginator;
import com.hanielfialho.menuframework.api.pagination.async.PageSource;
import com.hanielfialho.menuframework.api.pagination.async.PageStateAdapter;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/** Exemplo completo de paginação assíncrona com retry. */
public final class AsyncProductMenu implements Menu<AsyncProductMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(6);

  private static final PaginationLayout PAGINATION =
      PaginationLayout.builder(LAYOUT)
          .contentArea(1, 1, 4, 7)
          .previousSlot(5, 0)
          .indicatorSlot(5, 4)
          .nextSlot(5, 8)
          .build();

  private static final int CLOSE_SLOT = LAYOUT.slot(5, 3);

  private static final PageStateAdapter<State, Product> PAGE_ADAPTER =
      new PageStateAdapter<>() {
        @Override
        public AsyncPageState<Product> pageState(State state) {
          return state.products();
        }

        @Override
        public State withPageState(State state, AsyncPageState<Product> pageState) {
          return state.withProducts(pageState);
        }
      };

  private final AsyncPaginator<Product> paginator;

  public AsyncProductMenu(PageSource<Product> source) {
    this.paginator = AsyncPaginator.create("products", source);
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Produtos assíncronos", NamedTextColor.GOLD);
  }

  @Override
  public void onOpen(MenuOpenContext<State> context) {
    this.paginator.load(context, PAGE_ADAPTER, context.state().products().request());
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    canvas.background(ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));

    AsyncPageState<Product> state = context.state().products();

    switch (state.status()) {
      case LOADING -> {
        PAGINATION.contentSlots().forEach(canvas::empty);
        canvas.item(
            PAGINATION.previousSlot(),
            ItemStacks.named(
                Material.GRAY_DYE, Component.text("Carregando", NamedTextColor.DARK_GRAY)));
        canvas.item(
            PAGINATION.nextSlot(),
            ItemStacks.named(
                Material.GRAY_DYE, Component.text("Carregando", NamedTextColor.DARK_GRAY)));
        canvas.item(
            PAGINATION.indicatorSlot().orElseThrow(),
            ItemStacks.named(
                Material.CLOCK, Component.text("Carregando...", NamedTextColor.YELLOW)));
      }

      case READY -> {
        PageSlice<Product> page = state.requirePage();

        PAGINATION.forEachEntry(
            page,
            (slot, product, indexInPage, absoluteIndex) ->
                canvas.button(
                    slot,
                    ItemStacks.named(
                        product.material(),
                        Component.text(product.name(), NamedTextColor.GREEN),
                        List.of(Component.text("Preço: " + product.price(), NamedTextColor.GOLD))),
                    interaction ->
                        interaction
                            .viewer()
                            .sendMessage(
                                Component.text(
                                    "Selecionado: " + product.name(), NamedTextColor.GREEN))));

        PAGINATION.forEachUnusedSlot(page, canvas::empty);

        if (!page.hasPrevious()) {
          canvas.item(
              PAGINATION.previousSlot(),
              ItemStacks.named(
                  Material.GRAY_DYE, Component.text("Primeira página", NamedTextColor.DARK_GRAY)));
        }

        if (page.hasPrevious()) {
          canvas.button(
              PAGINATION.previousSlot(),
              ItemStacks.named(
                  Material.ARROW, Component.text("Página anterior", NamedTextColor.YELLOW)),
              interaction ->
                  this.paginator.load(
                      interaction, PAGE_ADAPTER, PAGINATION, page.cursor().previous()));
        }

        if (!page.hasNext()) {
          canvas.item(
              PAGINATION.nextSlot(),
              ItemStacks.named(
                  Material.GRAY_DYE, Component.text("Última página", NamedTextColor.DARK_GRAY)));
        }

        if (page.hasNext()) {
          canvas.button(
              PAGINATION.nextSlot(),
              ItemStacks.named(
                  Material.ARROW, Component.text("Próxima página", NamedTextColor.YELLOW)),
              interaction ->
                  this.paginator.load(interaction, PAGE_ADAPTER, PAGINATION, page.cursor().next()));
        }

        canvas.item(
            PAGINATION.indicatorSlot().orElseThrow(),
            ItemStacks.named(
                Material.PAPER, Component.text("Página " + page.number(), NamedTextColor.AQUA)));
      }

      case ERROR -> {
        PAGINATION.contentSlots().forEach(canvas::empty);
        canvas.item(
            PAGINATION.previousSlot(),
            ItemStacks.named(
                Material.GRAY_DYE, Component.text("Indisponível", NamedTextColor.DARK_GRAY)));
        canvas.item(
            PAGINATION.nextSlot(),
            ItemStacks.named(
                Material.GRAY_DYE, Component.text("Indisponível", NamedTextColor.DARK_GRAY)));
        canvas.button(
            PAGINATION.indicatorSlot().orElseThrow(),
            ItemStacks.named(
                Material.REDSTONE_BLOCK,
                Component.text("Falha ao carregar", NamedTextColor.RED),
                List.of(
                    Component.text("Clique para tentar novamente", NamedTextColor.YELLOW),
                    Component.text(
                        "Tipo: " + state.requireError().exceptionType(),
                        NamedTextColor.DARK_GRAY))),
            interaction -> this.paginator.reload(interaction, PAGE_ADAPTER));
      }
    }

    canvas.button(
        CLOSE_SLOT,
        ItemStacks.named(Material.BARRIER, Component.text("Fechar", NamedTextColor.RED)),
        MenuInteraction::close);
  }

  public record State(String filter, AsyncPageState<Product> products) {

    public State {
      Objects.requireNonNull(filter, "filter");
      Objects.requireNonNull(products, "products");
    }

    public static State initial(String filter) {
      PageRequest request = PAGINATION.request(PageCursor.FIRST);
      return new State(filter, AsyncPageState.initial(request));
    }

    public State withProducts(AsyncPageState<Product> products) {
      return new State(this.filter, Objects.requireNonNull(products, "products"));
    }
  }
}
