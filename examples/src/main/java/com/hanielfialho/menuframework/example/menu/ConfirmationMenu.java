package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuButton;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import com.hanielfialho.menuframework.api.feedback.StandardMenuFeedbackSignals;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Exemplo direto de confirmação antes de executar uma ação. */
public final class ConfirmationMenu implements Menu<ConfirmationMenu.State> {

  private static final MenuLayout LAYOUT =
      MenuLayout.chestBuilder(3).slot("confirm", 11).slot("message", 13).slot("cancel", 15).build();

  private final Consumer<Player> confirmAction;

  public ConfirmationMenu(Consumer<Player> confirmAction) {
    this.confirmAction = Objects.requireNonNull(confirmAction, "confirmAction");
  }

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(@NonNull MenuRenderContext<State> context) {
    return Component.text("Confirmação", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    canvas.component(context, MenuComponents.background());

    canvas.item(
        "message",
        ItemStacks.named(
            Material.PAPER, Component.text(context.state().message(), NamedTextColor.AQUA)));

    canvas.component(
        context,
        MenuButton.<State>at("confirm")
            .icon(
                ItemStacks.named(
                    Material.LIME_CONCRETE, Component.text("Confirmar", NamedTextColor.GREEN)))
            .feedback(StandardMenuFeedbackSignals.ACTION_SUCCESS)
            .onClick(
                interaction -> {
                  this.confirmAction.accept(interaction.viewer());
                  interaction.close();
                })
            .build());
    canvas.component(
        context,
        MenuButton.<State>at("cancel")
            .icon(
                ItemStacks.named(
                    Material.RED_CONCRETE, Component.text("Cancelar", NamedTextColor.RED)))
            .feedback(StandardMenuFeedbackSignals.NAVIGATION_BACK)
            .onClick(interaction -> interaction.backOrClose())
            .build());
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
