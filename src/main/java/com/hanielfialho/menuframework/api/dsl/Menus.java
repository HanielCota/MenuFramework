package com.hanielfialho.menuframework.api.dsl;

import com.hanielfialho.menuframework.api.EmptyMenuState;
import com.hanielfialho.menuframework.api.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Factory methods for common menu patterns. */
public final class Menus {

  private Menus() {}

  /**
   * Creates a confirmation menu with confirm and cancel buttons.
   *
   * @param title menu title
   * @param message message shown between the buttons
   * @param onConfirm action when confirm is clicked
   * @return confirmation menu
   */
  public static Menu<EmptyMenuState> confirmation(
      String title, String message, Consumer<Player> onConfirm) {
    return confirmation(title, message, onConfirm, player -> {});
  }

  /**
   * Creates a confirmation menu with confirm, cancel and both callbacks.
   *
   * @param title menu title
   * @param message message shown between the buttons
   * @param onConfirm action when confirm is clicked
   * @param onCancel action when cancel is clicked
   * @return confirmation menu
   */
  public static Menu<EmptyMenuState> confirmation(
      String title, String message, Consumer<Player> onConfirm, Consumer<Player> onCancel) {
    Objects.requireNonNull(onConfirm, "onConfirm");
    Objects.requireNonNull(onCancel, "onCancel");

    return MenuBuilder.<EmptyMenuState>chest(3, title)
        .background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .item(
            1,
            4,
            ctx -> {
              ItemStack item = new ItemStack(Material.PAPER);
              item.editMeta(meta -> meta.displayName(Component.text(message, NamedTextColor.AQUA)));
              return item;
            })
        .button(
            1,
            2,
            ctx -> {
              ItemStack item = new ItemStack(Material.LIME_CONCRETE);
              item.editMeta(
                  meta -> meta.displayName(Component.text("Confirmar", NamedTextColor.GREEN)));
              return item;
            },
            interaction -> {
              onConfirm.accept(interaction.viewer());
              interaction.close();
            })
        .button(
            1,
            6,
            ctx -> {
              ItemStack item = new ItemStack(Material.RED_CONCRETE);
              item.editMeta(
                  meta -> meta.displayName(Component.text("Cancelar", NamedTextColor.RED)));
              return item;
            },
            interaction -> {
              onCancel.accept(interaction.viewer());
              interaction.backOrClose();
            })
        .build();
  }
}
