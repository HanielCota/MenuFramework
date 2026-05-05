package com.github.hanielcota.menuframework.messaging;

/** Keys for localized messages used throughout the framework. */
public enum MessageKey {
  MENU_OPEN_ERROR("Erro ao abrir menu: {0}"),
  MENU_OPEN_ERROR_FALLBACK("Não foi possível abrir o menu.");

  private final String defaultMessage;

  MessageKey(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }

  public String defaultMessage() {
    return defaultMessage;
  }
}
