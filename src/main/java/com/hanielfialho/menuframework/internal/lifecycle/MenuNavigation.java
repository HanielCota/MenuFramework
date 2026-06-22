package com.hanielfialho.menuframework.internal.lifecycle;

import com.hanielfialho.menuframework.api.Menu;
import java.util.Objects;

/** Destino interno de uma navegação para frente. */
public record MenuNavigation<T>(Menu<T> menu, T initialState) {

  public MenuNavigation {
    Objects.requireNonNull(menu, "menu");
    Objects.requireNonNull(initialState, "initialState");
  }
}
