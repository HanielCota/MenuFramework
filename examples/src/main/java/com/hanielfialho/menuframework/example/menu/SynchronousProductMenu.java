package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.pagination.PageCursor;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import com.hanielfialho.menuframework.api.pagination.PaginationLayout;
import com.hanielfialho.menuframework.api.pagination.Paginator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/** Exemplo completo de paginação síncrona. */
public final class SynchronousProductMenu implements Menu<SynchronousProductMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(6);

  private static final PaginationLayout PAGINATION =
      PaginationLayout.builder(LAYOUT)
          .contentArea(1, 1, 4, 7)
          .previousSlot(5, 0)
          .indicatorSlot(5, 4)
          .nextSlot(5, 8)
          .build();

  private static final int CLOSE_SLOT = LAYOUT.slot(5, 3);

  private final Paginator<Product> products;

  public SynchronousProductMenu(Collection<Product> products) {
    this.products = Paginator.copyOf(products);
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Produtos", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    PageSlice<Product> page = this.products.page(PAGINATION.request(context.state().cursor()));

    canvas.background(ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));

    PAGINATION.forEachEntry(
        page,
        (slot, product, indexInPage, absoluteIndex) ->
            canvas.button(
                slot,
                ItemStacks.named(
                    product.material(),
                    Component.text(product.name(), NamedTextColor.GREEN),
                    List.of(
                        Component.text(
                            "Posição: " + (absoluteIndex + 1L), NamedTextColor.DARK_GRAY),
                        Component.text("Preço: " + product.price(), NamedTextColor.GOLD))),
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
              interaction.updateState(state -> state.withCursor(page.cursor().previous())));
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
          ItemStacks.named(Material.ARROW, Component.text("Próxima página", NamedTextColor.YELLOW)),
          interaction -> interaction.updateState(state -> state.withCursor(page.cursor().next())));
    }

    canvas.item(
        PAGINATION.indicatorSlot().orElseThrow(),
        ItemStacks.named(
            Material.PAPER,
            Component.text(
                "Página " + page.number() + " de " + page.totalPages().orElseThrow(),
                NamedTextColor.AQUA),
            List.of(
                Component.text(
                    "Itens: " + page.totalElements().orElseThrow(), NamedTextColor.GRAY))));

    canvas.button(
        CLOSE_SLOT,
        ItemStacks.named(Material.BARRIER, Component.text("Fechar", NamedTextColor.RED)),
        MenuInteraction::close);
  }

  public record State(PageCursor cursor) {

    public State {
      Objects.requireNonNull(cursor, "cursor");
    }

    public static State firstPage() {
      return new State(PageCursor.FIRST);
    }

    public State withCursor(PageCursor cursor) {
      return new State(Objects.requireNonNull(cursor, "cursor"));
    }
  }
}
