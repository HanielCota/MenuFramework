package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
@SuppressWarnings("UnstableApiUsage")
final class MenuListenerIntegrationTest extends MenuManagerTestSupport {

  @Test
  void readOnlyTopClickIsCancelledAndDispatched() {
    RecordingMenu menu = new RecordingMenu("Read only");
    this.open(menu, MenuState.initial());

    InventoryClickEvent event =
        this.fireClick(PRIMARY_SLOT, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertTrue(event.isCancelled());
    ItemStack primaryItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(primaryItem);
    assertEquals(Material.DIAMOND, primaryItem.getType());
  }

  @Test
  void previouslyCancelledClickDoesNotDispatchButton() {
    RecordingMenu menu = new RecordingMenu("Cancelled");
    this.open(menu, MenuState.initial());

    InventoryClickEvent event =
        this.createClick(PRIMARY_SLOT, ClickType.LEFT, InventoryAction.PICKUP_ALL);
    event.setCancelled(true);

    this.server.getPluginManager().callEvent(event);

    assertTrue(event.isCancelled());
    ItemStack primaryItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(primaryItem);
    assertEquals(Material.STONE, primaryItem.getType());
  }

  @Test
  void readOnlyPolicyBlocksPlayerInventoryClicks() {
    RecordingMenu menu = new RecordingMenu("Read only");
    this.open(menu, MenuState.initial());

    int bottomRawSlot = this.topSize();

    InventoryClickEvent event =
        this.fireClick(bottomRawSlot, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertTrue(event.isCancelled());
  }

  @Test
  void playerInventoryPolicyAllowsOrdinaryBottomClick() {
    RecordingMenu menu =
        new RecordingMenu("Player inventory", InteractionPolicy.PLAYER_INVENTORY_ALLOWED);
    this.open(menu, MenuState.initial());

    InventoryClickEvent event =
        this.fireClick(this.topSize(), ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertFalse(event.isCancelled());
  }

  @Test
  void playerInventoryPolicyStillBlocksShiftTransfer() {
    RecordingMenu menu =
        new RecordingMenu("Player inventory", InteractionPolicy.PLAYER_INVENTORY_ALLOWED);
    this.open(menu, MenuState.initial());

    InventoryClickEvent event =
        this.fireClick(
            this.topSize(), ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);

    assertTrue(event.isCancelled());
  }

  @Test
  void topClickAlwaysRemainsProtectedWithPlayerInventoryPolicy() {
    RecordingMenu menu =
        new RecordingMenu("Player inventory", InteractionPolicy.PLAYER_INVENTORY_ALLOWED);
    this.open(menu, MenuState.initial());

    InventoryClickEvent event =
        this.fireClick(PRIMARY_SLOT, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    assertTrue(event.isCancelled());
    ItemStack primaryItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(primaryItem);
    assertEquals(Material.DIAMOND, primaryItem.getType());
  }

  @Test
  void readOnlyPolicyCancelsEveryDrag() {
    RecordingMenu menu = new RecordingMenu("Read only");
    this.open(menu, MenuState.initial());

    InventoryDragEvent event = this.fireDrag(Map.of(this.topSize(), new ItemStack(Material.STONE)));

    assertTrue(event.isCancelled());
  }

  @Test
  void playerInventoryPolicyAllowsBottomOnlyDrag() {
    RecordingMenu menu =
        new RecordingMenu("Player inventory", InteractionPolicy.PLAYER_INVENTORY_ALLOWED);
    this.open(menu, MenuState.initial());

    InventoryDragEvent event = this.fireDrag(Map.of(this.topSize(), new ItemStack(Material.STONE)));

    assertFalse(event.isCancelled());
  }

  @Test
  void playerInventoryPolicyBlocksDragThatTouchesTheMenu() {
    RecordingMenu menu =
        new RecordingMenu("Player inventory", InteractionPolicy.PLAYER_INVENTORY_ALLOWED);
    this.open(menu, MenuState.initial());

    InventoryDragEvent event =
        this.fireDrag(
            Map.of(
                0, new ItemStack(Material.STONE), this.topSize(), new ItemStack(Material.STONE)));

    assertTrue(event.isCancelled());
  }

  private InventoryClickEvent fireClick(int rawSlot, ClickType clickType, InventoryAction action) {
    InventoryClickEvent event = this.createClick(rawSlot, clickType, action);

    this.server.getPluginManager().callEvent(event);
    return event;
  }

  private InventoryClickEvent createClick(
      int rawSlot, ClickType clickType, InventoryAction action) {
    return new InventoryClickEvent(
        this.player.getOpenInventory(),
        InventoryType.SlotType.CONTAINER,
        rawSlot,
        clickType,
        action);
  }

  private InventoryDragEvent fireDrag(Map<Integer, ItemStack> newItems) {
    InventoryView view = this.player.getOpenInventory();
    ItemStack cursor = new ItemStack(Material.STONE);

    InventoryDragEvent event =
        new InventoryDragEvent(view, cursor.clone(), cursor, false, newItems);

    this.server.getPluginManager().callEvent(event);
    return event;
  }

  private int topSize() {
    return this.player.getOpenInventory().getTopInventory().getSize();
  }
}
