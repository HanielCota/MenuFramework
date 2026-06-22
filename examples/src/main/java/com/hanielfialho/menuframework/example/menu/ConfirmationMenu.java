package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Exemplo direto de confirmação antes de executar uma ação. */
public final class ConfirmationMenu implements Menu<ConfirmationMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(3);

  private final Consumer<Player> confirmAction;

  public ConfirmationMenu(Consumer<Player> confirmAction) {
    this.confirmAction = Objects.requireNonNull(confirmAction, "confirmAction");
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Confirmação", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    canvas.background(ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));

    canvas.item(
        13,
        ItemStacks.named(
            Material.PAPER, Component.text(context.state().message(), NamedTextColor.AQUA)));

    canvas.button(
        11,
        ItemStacks.named(Material.LIME_CONCRETE, Component.text("Confirmar", NamedTextColor.GREEN)),
        interaction -> {
          this.confirmAction.accept(interaction.viewer());
          interaction.close();
        });

    canvas.button(
        15,
        ItemStacks.named(Material.RED_CONCRETE, Component.text("Cancelar", NamedTextColor.RED)),
        MenuInteraction::backOrClose);
  }

  public record State(String message) {

    public State {
      Objects.requireNonNull(message, "message");

      if (message.isBlank()) {
        throw new IllegalArgumentException("message cannot be blank");
      }
    }
  }
}
