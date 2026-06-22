package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuButton;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;

/** Exemplo simples de menu de configurações com estado imutável. */
public final class SettingsMenu implements Menu<SettingsMenu.State> {

  private static final MenuLayout LAYOUT =
      MenuLayout.chestBuilder(3)
          .slot("sounds", 10)
          .slot("particles", 13)
          .slot("compact", 16)
          .slot("close", 22)
          .build();

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public InteractionPolicy interactionPolicy() {
    return InteractionPolicy.PLAYER_INVENTORY_ALLOWED;
  }

  @Override
  public Component title(@NonNull MenuRenderContext<State> context) {
    return Component.text("Configurações", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.component(context, MenuComponents.background());
    canvas.component(
        context,
        MenuButton.<State>at("sounds")
            .icon(toggleIcon("Sons", state.sounds(), "ligados", "desligados"))
            .onClick(
                interaction ->
                    interaction.updateState(
                        current ->
                            new State(
                                !current.sounds(), current.particles(), current.compactMode())))
            .build());
    canvas.component(
        context,
        MenuButton.<State>at("particles")
            .icon(toggleIcon("Partículas", state.particles(), "ligadas", "desligadas"))
            .onClick(
                interaction ->
                    interaction.updateState(
                        current ->
                            new State(
                                current.sounds(), !current.particles(), current.compactMode())))
            .build());
    canvas.component(
        context,
        MenuButton.<State>at("compact")
            .icon(toggleIcon("Modo compacto", state.compactMode(), "ligado", "desligado"))
            .onClick(
                interaction ->
                    interaction.updateState(
                        current ->
                            new State(
                                current.sounds(), current.particles(), !current.compactMode())))
            .build());
    canvas.component(context, MenuComponents.closeButton("close"));
  }

  private static org.bukkit.inventory.ItemStack toggleIcon(
      String label, boolean enabled, String enabledText, String disabledText) {
    return ItemStacks.named(
        enabled ? Material.LIME_DYE : Material.GRAY_DYE,
        Component.text(
            label + ": " + (enabled ? enabledText : disabledText),
            enabled ? NamedTextColor.GREEN : NamedTextColor.GRAY));
  }

  public record State(boolean sounds, boolean particles, boolean compactMode) {}
}
