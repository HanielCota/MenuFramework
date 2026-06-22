package com.hanielfialho.menuframework.internal.inventory;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import java.util.Objects;
import java.util.Set;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

/**
 * Regras puras utilizadas pelo listener para decidir se uma interação pode alcançar algum slot
 * pertencente ao menu.
 */
public final class InventoryInteractionGuard {

  private static final ClickDecision ALLOW = new ClickDecision(false, false);

  private static final ClickDecision CANCEL = new ClickDecision(true, false);

  private static final ClickDecision CANCEL_AND_DISPATCH = new ClickDecision(true, true);

  private InventoryInteractionGuard() {}

  public static ClickDecision decideClick(
      InteractionPolicy policy, ClickArea area, ClickType clickType, InventoryAction action) {
    Objects.requireNonNull(policy, "policy");
    Objects.requireNonNull(area, "area");
    Objects.requireNonNull(clickType, "clickType");
    Objects.requireNonNull(action, "action");

    if (area == ClickArea.TOP) {
      /*
       * O clique sempre é cancelado no inventário do menu. Tipos ainda
       * desconhecidos são bloqueados sem executar o handler do botão.
       */
      if (clickType == ClickType.UNKNOWN || action == InventoryAction.UNKNOWN) {
        return CANCEL;
      }

      return CANCEL_AND_DISPATCH;
    }

    if (policy == InteractionPolicy.READ_ONLY) {
      return CANCEL;
    }

    return switch (area) {
      case PLAYER ->
          isPlayerInventoryActionSafe(action) && clickType != ClickType.UNKNOWN ? ALLOW : CANCEL;

      case OUTSIDE ->
          isOutsideActionSafe(action) && clickType != ClickType.UNKNOWN ? ALLOW : CANCEL;

      case UNKNOWN -> CANCEL;
      case TOP -> throw new AssertionError("TOP was handled above");
    };
  }

  public static boolean shouldCancelDrag(
      InteractionPolicy policy, int topInventorySize, Set<Integer> rawSlots) {
    Objects.requireNonNull(policy, "policy");
    Objects.requireNonNull(rawSlots, "rawSlots");

    if (topInventorySize <= 0) {
      throw new IllegalArgumentException(
          "topInventorySize must be greater than zero: " + topInventorySize);
    }

    if (policy == InteractionPolicy.READ_ONLY) {
      return true;
    }

    return rawSlots.stream().anyMatch(slot -> slot < topInventorySize);
  }

  private static boolean isOutsideActionSafe(InventoryAction action) {
    return switch (action) {
      case NOTHING, DROP_ALL_CURSOR, DROP_ONE_CURSOR -> true;

      default -> false;
    };
  }

  private static boolean isPlayerInventoryActionSafe(InventoryAction action) {
    return switch (action) {
      case NOTHING,
          PICKUP_ALL,
          PICKUP_SOME,
          PICKUP_HALF,
          PICKUP_ONE,
          PLACE_ALL,
          PLACE_SOME,
          PLACE_ONE,
          SWAP_WITH_CURSOR,
          DROP_ALL_CURSOR,
          DROP_ONE_CURSOR,
          DROP_ALL_SLOT,
          DROP_ONE_SLOT,
          HOTBAR_SWAP,
          CLONE_STACK ->
          true;

      case MOVE_TO_OTHER_INVENTORY, COLLECT_TO_CURSOR, UNKNOWN -> false;

      /*
       * A comparação por nome evita acoplar a compilação a ações
       * adicionadas ou removidas entre versões da Paper API.
       */
      default ->
          switch (action.name()) {
            case "HOTBAR_MOVE_AND_READD",
                "PICKUP_FROM_BUNDLE",
                "PICKUP_ALL_INTO_BUNDLE",
                "PICKUP_SOME_INTO_BUNDLE",
                "PLACE_FROM_BUNDLE",
                "PLACE_ALL_INTO_BUNDLE",
                "PLACE_SOME_INTO_BUNDLE" ->
                true;

            default -> false;
          };
    };
  }

  public enum ClickArea {
    TOP,
    PLAYER,
    OUTSIDE,
    UNKNOWN
  }

  public record ClickDecision(boolean cancel, boolean dispatchButton) {}
}
