package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
final class MenuFrameRecoveryTest extends MenuManagerTestSupport {

  @Test
  void refreshRepairsAVisualMutationOutsideTheLastCommittedFrame() {
    RecordingMenu menu = new RecordingMenu("Recovery");
    this.open(menu, MenuState.initial());

    this.player
        .getOpenInventory()
        .getTopInventory()
        .setItem(PRIMARY_SLOT, new ItemStack(Material.DIRT));

    ItemStack mutatedItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(mutatedItem);
    assertEquals(Material.DIRT, mutatedItem.getType());

    assertTrue(this.menus.refresh(this.player));
    this.advanceTicks(2L);

    ItemStack restoredItem = this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT);
    assertNotNull(restoredItem);
    assertEquals(Material.STONE, restoredItem.getType());
  }
}
