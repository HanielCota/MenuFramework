package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/** Exemplo simples de menu de configurações com estado imutável. */
public final class SettingsMenu implements Menu<SettingsMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(3);

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public InteractionPolicy interactionPolicy() {
    return InteractionPolicy.PLAYER_INVENTORY_ALLOWED;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Configurações", NamedTextColor.GOLD);
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.background(ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));

    canvas.button(
        10,
        ItemStacks.named(
            state.sounds() ? Material.LIME_DYE : Material.GRAY_DYE,
            Component.text(
                "Sons: " + (state.sounds() ? "ligados" : "desligados"),
                state.sounds() ? NamedTextColor.GREEN : NamedTextColor.GRAY)),
        interaction ->
            interaction.updateState(
                current ->
                    new State(!current.sounds(), current.particles(), current.compactMode())));

    canvas.button(
        13,
        ItemStacks.named(
            state.particles() ? Material.LIME_DYE : Material.GRAY_DYE,
            Component.text(
                "Partículas: " + (state.particles() ? "ligadas" : "desligadas"),
                state.particles() ? NamedTextColor.GREEN : NamedTextColor.GRAY)),
        interaction ->
            interaction.updateState(
                current ->
                    new State(current.sounds(), !current.particles(), current.compactMode())));

    canvas.button(
        16,
        ItemStacks.named(
            state.compactMode() ? Material.LIME_DYE : Material.GRAY_DYE,
            Component.text(
                "Modo compacto: " + (state.compactMode() ? "ligado" : "desligado"),
                state.compactMode() ? NamedTextColor.GREEN : NamedTextColor.GRAY)),
        interaction ->
            interaction.updateState(
                current ->
                    new State(current.sounds(), current.particles(), !current.compactMode())));

    canvas.button(
        22,
        ItemStacks.named(Material.BARRIER, Component.text("Fechar", NamedTextColor.RED)),
        MenuInteraction::close);
  }

  public record State(boolean sounds, boolean particles, boolean compactMode) {}
}
