package com.hanielfialho.menuframework.internal.inventory;

import static com.hanielfialho.menuframework.internal.inventory.InventoryInteractionGuard.ClickArea.OUTSIDE;
import static com.hanielfialho.menuframework.internal.inventory.InventoryInteractionGuard.ClickArea.PLAYER;
import static com.hanielfialho.menuframework.internal.inventory.InventoryInteractionGuard.ClickArea.TOP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import java.util.Set;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.junit.jupiter.api.Test;

final class InventoryInteractionGuardTest {

  @Test
  void validTopClickIsCancelledAndDispatched() {
    InventoryInteractionGuard.ClickDecision decision =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.READ_ONLY, TOP, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertTrue(decision.cancel());
    assertTrue(decision.dispatchButton());
  }

  @Test
  void unknownTopClickIsCancelledWithoutDispatch() {
    InventoryInteractionGuard.ClickDecision decision =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.READ_ONLY, TOP, ClickType.UNKNOWN, InventoryAction.UNKNOWN);

    assertTrue(decision.cancel());
    assertFalse(decision.dispatchButton());
  }

  @Test
  void readOnlyPolicyBlocksPlayerInventory() {
    InventoryInteractionGuard.ClickDecision decision =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.READ_ONLY, PLAYER, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertTrue(decision.cancel());
    assertFalse(decision.dispatchButton());
  }

  @Test
  void playerInventoryPolicyAllowsLocalActions() {
    InventoryInteractionGuard.ClickDecision decision =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED,
            PLAYER,
            ClickType.LEFT,
            InventoryAction.PICKUP_ALL);

    assertFalse(decision.cancel());
    assertFalse(decision.dispatchButton());
  }

  @Test
  void playerInventoryPolicyBlocksActionsThatCanReachTopInventory() {
    InventoryInteractionGuard.ClickDecision shiftClick =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED,
            PLAYER,
            ClickType.SHIFT_LEFT,
            InventoryAction.MOVE_TO_OTHER_INVENTORY);

    InventoryInteractionGuard.ClickDecision collect =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED,
            PLAYER,
            ClickType.DOUBLE_CLICK,
            InventoryAction.COLLECT_TO_CURSOR);

    assertTrue(shiftClick.cancel());
    assertTrue(collect.cancel());
  }

  @Test
  void outsideCursorDropIsAllowedOnlyByPlayerInventoryPolicy() {
    InventoryInteractionGuard.ClickDecision allowed =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED,
            OUTSIDE,
            ClickType.LEFT,
            InventoryAction.DROP_ONE_CURSOR);

    InventoryInteractionGuard.ClickDecision blocked =
        InventoryInteractionGuard.decideClick(
            InteractionPolicy.READ_ONLY, OUTSIDE, ClickType.LEFT, InventoryAction.DROP_ONE_CURSOR);

    assertFalse(allowed.cancel());
    assertTrue(blocked.cancel());
  }

  @Test
  void dragIsAllowedOnlyWhenItStaysBelowTopInventory() {
    assertTrue(
        InventoryInteractionGuard.shouldCancelDrag(
            InteractionPolicy.READ_ONLY, 27, Set.of(30, 31)));

    assertFalse(
        InventoryInteractionGuard.shouldCancelDrag(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED, 27, Set.of(27, 30, 53)));

    assertTrue(
        InventoryInteractionGuard.shouldCancelDrag(
            InteractionPolicy.PLAYER_INVENTORY_ALLOWED, 27, Set.of(26, 27, 30)));
  }
}
