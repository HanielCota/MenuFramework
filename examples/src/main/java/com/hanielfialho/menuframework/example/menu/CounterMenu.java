package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/** Menu mínimo com estado imutável e navegação. */
public final class CounterMenu implements Menu<CounterMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(3);

  private final SynchronousProductMenu productMenu;

  public CounterMenu(SynchronousProductMenu productMenu) {
    this.productMenu = productMenu;
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Contador", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.background(ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));

    canvas.button(
        11,
        ItemStacks.named(
            Material.EMERALD, Component.text("Cliques: " + state.clicks(), NamedTextColor.GREEN)),
        interaction -> interaction.updateState(State::increment));

    canvas.button(
        15,
        ItemStacks.named(Material.CHEST, Component.text("Produtos", NamedTextColor.YELLOW)),
        interaction ->
            interaction.open(this.productMenu, SynchronousProductMenu.State.firstPage()));

    canvas.button(
        22,
        ItemStacks.named(Material.BARRIER, Component.text("Fechar", NamedTextColor.RED)),
        MenuInteraction::close);
  }

  public record State(int clicks) {

    public State {
      if (clicks < 0) {
        throw new IllegalArgumentException("clicks must be >= 0: " + clicks);
      }
    }

    public State increment() {
      return new State(Math.incrementExact(this.clicks));
    }
  }
}
