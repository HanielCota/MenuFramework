package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.component.CountdownComponent;

/** Exemplo simplificado de contagem regressiva usando o componente de alto nível. */
public final class SimpleCountdownMenu {

  private SimpleCountdownMenu() {}

  /**
   * Cria um menu de contagem regressiva.
   *
   * @param title titulo do menu
   * @param initialSeconds segundos iniciais
   * @return menu de contagem regressiva
   */
  public static Menu<Integer> create(String title, int initialSeconds) {
    return CountdownComponent.<Integer>builder(title)
        .secondsReader(seconds -> seconds)
        .stateFactory(seconds -> seconds)
        .onFinish(finished -> {})
        .build();
  }
}
