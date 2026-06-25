package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.EmptyMenuState;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.dsl.Menus;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/** Exemplo simplificado de menu de confirmação usando a factory declarativa. */
public final class SimpleConfirmationMenu {

  private SimpleConfirmationMenu() {}

  /**
   * Cria um menu de confirmação reutilizável.
   *
   * @param title titulo do menu
   * @param message mensagem exibida
   * @param onConfirm acao ao confirmar
   * @return menu de confirmação
   */
  public static Menu<EmptyMenuState> create(
      String title, String message, Consumer<Player> onConfirm) {
    return Menus.confirmation(title, message, onConfirm);
  }
}
