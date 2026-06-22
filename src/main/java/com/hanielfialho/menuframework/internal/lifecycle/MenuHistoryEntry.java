package com.hanielfialho.menuframework.internal.lifecycle;

import com.hanielfialho.menuframework.api.Menu;
import java.util.Objects;

/**
 * Snapshot lógico de um menu que pode ser reconstruído posteriormente.
 *
 * <p>O estado deve seguir o mesmo contrato do restante do framework: ser imutável ou tratado como
 * imutável pelo consumidor.
 */
public record MenuHistoryEntry<S>(Menu<S> menu, S state) {

  public MenuHistoryEntry {
    Objects.requireNonNull(menu, "menu");
    Objects.requireNonNull(state, "state");
  }
}
