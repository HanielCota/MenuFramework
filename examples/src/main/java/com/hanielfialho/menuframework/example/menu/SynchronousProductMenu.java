package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuComponents;
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
import org.jspecify.annotations.NonNull;

/** Exemplo completo de paginação síncrona. */
public final class SynchronousProductMenu implements Menu<SynchronousProductMenu.State> {

  private static final MenuLayout LAYOUT =
      MenuLayout.chestBuilder(6)
          .slot("previous", 5, 0)
          .slot("close", 5, 3)
          .slot("indicator", 5, 4)
          .slot("next", 5, 8)
          .build();

  private static final PaginationLayout PAGINATION =
      PaginationLayout.builder(LAYOUT)
          .contentArea(1, 1, 4, 7)
          .previousSlot(5, 0)
          .indicatorSlot(5, 4)
          .nextSlot(5, 8)
          .build();

  private final Paginator<Product> products;

  public SynchronousProductMenu(Collection<Product> products) {
    this.products = Paginator.copyOf(products);
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(@NonNull MenuRenderContext<State> context) {
    return Component.text("Produtos", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    PageSlice<Product> page = this.products.page(PAGINATION.request(context.state().cursor()));

    canvas.component(context, MenuComponents.background());

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

    canvas.component(
        context,
        MenuComponents.previousPageButton(
            "previous",
            ignored -> page.hasPrevious(),
            interaction ->
                interaction.updateState(state -> state.withCursor(page.cursor().previous()))));
    canvas.component(
        context,
        MenuComponents.nextPageButton(
            "next",
            ignored -> page.hasNext(),
            interaction ->
                interaction.updateState(state -> state.withCursor(page.cursor().next()))));

    canvas.item(
        "indicator",
        ItemStacks.named(
            Material.PAPER,
            Component.text(
                "Página " + page.number() + " de " + page.totalPages().orElseThrow(),
                NamedTextColor.AQUA),
            List.of(
                Component.text(
                    "Itens: " + page.totalElements().orElseThrow(), NamedTextColor.GRAY))));

    canvas.component(context, MenuComponents.closeButton("close"));
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
