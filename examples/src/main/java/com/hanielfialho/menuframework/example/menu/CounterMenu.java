package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuButton;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import com.hanielfialho.menuframework.api.feedback.StandardMenuFeedbackSignals;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;

/** Menu mínimo com estado imutável e navegação. */
public final class CounterMenu implements Menu<CounterMenu.State> {

  private static final MenuLayout LAYOUT =
      MenuLayout.chestBuilder(3).slot("counter", 11).slot("products", 15).slot("close", 22).build();

  private final SynchronousProductMenu productMenu;

  public CounterMenu(SynchronousProductMenu productMenu) {
    this.productMenu = productMenu;
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(@NonNull MenuRenderContext<State> context) {
    return Component.text("Contador", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.component(context, MenuComponents.background());
    canvas.component(
        context,
        MenuButton.<State>at("counter")
            .icon(
                ItemStacks.named(
                    Material.EMERALD,
                    Component.text("Cliques: " + state.clicks(), NamedTextColor.GREEN)))
            .onClick(interaction -> interaction.updateState(State::increment))
            .build());
    canvas.component(
        context,
        MenuButton.<State>at("products")
            .icon(
                ItemStacks.named(Material.CHEST, Component.text("Produtos", NamedTextColor.YELLOW)))
            .feedback(StandardMenuFeedbackSignals.NAVIGATION_FORWARD)
            .onClick(
                interaction ->
                    interaction.open(this.productMenu, SynchronousProductMenu.State.firstPage()))
            .build());
    canvas.component(context, MenuComponents.closeButton("close"));
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
