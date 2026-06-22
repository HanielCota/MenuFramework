package com.hanielfialho.menuframework.internal.inventory;

import com.hanielfialho.menuframework.api.MenuCloseReason;
import java.util.Objects;
import org.bukkit.event.inventory.InventoryCloseEvent;

/** Converte motivos Bukkit/Paper sem expor a enumeração da plataforma na API. */
public final class MenuCloseReasonMapper {

  private MenuCloseReasonMapper() {}

  public static MenuCloseReason map(InventoryCloseEvent.Reason reason) {
    Objects.requireNonNull(reason, "reason");

    /*
     * O nome é usado deliberadamente. Algumas versões de Paper removeram
     * ou marcaram motivos como deprecated; o fallback continua compilando
     * e converte valores futuros para UNKNOWN.
     */
    return switch (reason.name()) {
      case "PLAYER" -> MenuCloseReason.PLAYER;
      case "OPEN_NEW" -> MenuCloseReason.REPLACED;
      case "DISCONNECT" -> MenuCloseReason.QUIT;
      case "DEATH" -> MenuCloseReason.DEATH;
      case "TELEPORT" -> MenuCloseReason.TELEPORT;
      case "CANT_USE" -> MenuCloseReason.CANT_USE;
      case "UNLOADED" -> MenuCloseReason.UNLOADED;
      case "PLUGIN" -> MenuCloseReason.PLUGIN;
      default -> MenuCloseReason.UNKNOWN;
    };
  }
}
