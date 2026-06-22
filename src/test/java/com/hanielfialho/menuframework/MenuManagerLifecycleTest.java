package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
@SuppressWarnings("UnstableApiUsage")
final class MenuManagerLifecycleTest extends MenuManagerTestSupport {

  @Test
  void openRendersInitialFrameAndInvokesOnOpenOnce() {
    RecordingMenu menu = new RecordingMenu("Primary");
    MenuState initialState = MenuState.initial();

    this.open(menu, initialState);

    assertTrue(this.menus.isOpen(this.player));
    assertEquals(1, menu.openCount());
    assertEquals(0, menu.closeCount());
    assertEquals(initialState, menu.openedState());
    assertEquals(this.currentSessionId(), menu.openedSessionId());
    var primaryItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(primaryItem);
    assertEquals(Material.STONE, primaryItem.getType());
  }

  @Test
  void openingAnotherMenuReplacesThePreviousSession() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");

    this.open(first, MenuState.initial());
    var firstSessionId = this.currentSessionId();

    assertTrue(this.menus.open(this.player, second, new MenuState(7)));
    this.advanceTicks(2L);

    assertTrue(this.menus.isOpen(this.player));
    assertNotEquals(firstSessionId, this.currentSessionId());
    assertEquals(1, first.openCount());
    assertEquals(1, first.closeCount());
    assertEquals(List.of(MenuCloseReason.REPLACED), first.closeReasons());
    assertEquals(1, second.openCount());
    assertEquals(0, second.closeCount());
    assertEquals(new MenuState(7), second.openedState());
  }

  @Test
  void navigationClosesSourceWithNavigationReason() {
    RecordingMenu source = new RecordingMenu("Source");
    RecordingMenu destination = new RecordingMenu("Destination");

    source.onPrimary(interaction -> interaction.open(destination, new MenuState(3)));

    this.open(source, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(1, source.closeCount());
    assertEquals(List.of(MenuCloseReason.NAVIGATION), source.closeReasons());
    assertEquals(1, destination.openCount());
    assertEquals(new MenuState(3), destination.openedState());
    assertEquals(destination.openedSessionId(), this.currentSessionId());
  }

  @Test
  void closeButtonNotifiesExactlyOnceWithButtonReason() {
    RecordingMenu menu = new RecordingMenu("Closable");

    this.open(menu, MenuState.initial());
    this.dispatchCloseClick();

    assertTrue(this.menus.isOpen(this.player));

    this.advanceTicks(2L);

    assertFalse(this.menus.isOpen(this.player));
    assertEquals(1, menu.closeCount());
    assertEquals(List.of(MenuCloseReason.BUTTON), menu.closeReasons());

    this.advanceTicks(2L);
    assertEquals(1, menu.closeCount());
  }

  @Test
  void publicCloseUsesPluginReason() {
    RecordingMenu menu = new RecordingMenu("Plugin close");

    this.open(menu, new MenuState(2));

    assertTrue(this.menus.close(this.player));
    this.advanceTicks(2L);

    assertFalse(this.menus.isOpen(this.player));
    assertEquals(1, menu.closeCount());
    assertEquals(new MenuState(2), menu.closedState());
    assertEquals(List.of(MenuCloseReason.PLUGIN), menu.closeReasons());
  }

  @Test
  void inventoryCloseEventMapsPlayerReason() {
    RecordingMenu menu = new RecordingMenu("Manual close");

    this.open(menu, MenuState.initial());

    InventoryCloseEvent event =
        new InventoryCloseEvent(this.player.getOpenInventory(), InventoryCloseEvent.Reason.PLAYER);

    this.server.getPluginManager().callEvent(event);

    assertFalse(this.menus.isOpen(this.player));
    assertEquals(1, menu.closeCount());
    assertEquals(List.of(MenuCloseReason.PLAYER), menu.closeReasons());
  }

  @Test
  void quitTerminatesTheCurrentSession() {
    RecordingMenu menu = new RecordingMenu("Quit");

    this.open(menu, MenuState.initial());

    this.menus.handleQuit(this.player);

    assertFalse(this.menus.isOpen(this.player));
    assertEquals(1, menu.closeCount());
    assertEquals(List.of(MenuCloseReason.QUIT), menu.closeReasons());
  }

  @Test
  void shutdownIsIdempotentAndRejectsNewOperations() {
    RecordingMenu menu = new RecordingMenu("Shutdown");

    this.open(menu, MenuState.initial());

    this.framework.shutdown();
    this.framework.shutdown();

    assertTrue(this.framework.isShutdown());
    assertFalse(this.menus.isOpen(this.player));
    assertFalse(this.menus.open(this.player, new RecordingMenu("Rejected"), MenuState.initial()));
    assertFalse(this.menus.refresh(this.player));
    assertFalse(this.menus.close(this.player));

    /*
     * O shutdown não acessa a view nem executa callback de menu.
     * O descarte apenas invalida a sessão e cancela suas tasks.
     */
    assertEquals(0, menu.closeCount());
  }

  @Test
  void holderIdentityIsIsolatedBetweenFrameworkInstances() {
    RecordingMenu menu = new RecordingMenu("Isolated runtime");
    this.open(menu, MenuState.initial());

    MenuHolder holder =
        (MenuHolder) this.player.getOpenInventory().getTopInventory().getHolder(false);

    MenuFramework secondary = MenuFramework.create(this.plugin);

    try {
      assertTrue(this.menus.owns(holder));
      assertFalse(secondary.menus().owns(holder));
    } finally {
      secondary.shutdown();
    }
  }
}
